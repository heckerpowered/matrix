package heckerpowered.matrix.core.common.pool

import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

open class ObjectPool<T>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val lock = ReentrantLock(true)
    private val objects = ArrayDeque<T>()
    private val waiters = ArrayDeque<Waiter>()

    private sealed interface Waiter
    private inner class ContinuationWaiter(val continuation: Continuation<BorrowedObject>) : Waiter
    private inner class CallbackWaiter(val callback: (Result<BorrowedObject>) -> Unit) : Waiter

    inner class BorrowedObject internal constructor(value: T) : AutoCloseable {
        /** Holds the actual object while the lease is active; null after [recycle] or [release]. */
        private var valueOrNull: T? = value

        /**
         * The underlying pooled object.
         *
         * @throws IllegalStateException if the lease has already been recycled or released.
         */
        val value: T
            get() = valueOrNull ?: error("Borrowed object already released/returned")

        /**
         * Returns the object to the pool. Safe to call multiple times (idempotent).
         * This is equivalent to [recycle].
         */
        override fun close() {
            recycle()
        }

        /**
         * Steals the underlying object from the lease and prevents it from being returned to the pool.
         * After this call, [value] can no longer be accessed and [recycle] becomes a no-op.
         *
         * @return the underlying object that the caller now owns exclusively.
         * @throws IllegalStateException if the lease has already been recycled or released.
         */
        fun release(): T {
            val value = valueOrNull ?: error("Borrowed object already released/returned")
            valueOrNull = null
            return value
        }

        /**
         * Returns the object to the pool. Under the lock, the object is either delivered to the oldest waiter
         * or stored back to the available deque. Delivery is dispatched on [dispatcher]; if the targeted
         * continuation has been cancelled meanwhile, the delivery path retries with the next waiter or stores
         * the object back, ensuring no object loss.
         */
        fun recycle() {
            val value = valueOrNull ?: return
            valueOrNull = null

            val waiter = dequeueWaiterOrStore(value)
            if (waiter != null) {
                resumeWaiterOrFallback(waiter, value)
            }
        }

        @OptIn(ExperimentalContracts::class)
        inline fun <R> use(operation: (T) -> R): R {
            contract {
                callsInPlace(operation, InvocationKind.EXACTLY_ONCE)
            }

            return try {
                operation(value)
            } finally {
                close()
            }
        }

        /**
         * Processes a single waiter:
         *
         * - If the waiter is a live [ContinuationWaiter], resume it and return true.
         * - If the waiter is a cancelled [ContinuationWaiter], return false to request another waiter.
         * - If the waiter is a [CallbackWaiter], invoke the callback and return true.
         *
         * The function does not touch shared queues; it only performs delivery checks
         * and the actual hand-off (resume or callback).
         *
         * @return true if the value has been delivered; false if the caller should obtain the next waiter.
         */
        private fun stepDeliver(waiter: Waiter, value: T): Boolean = when (waiter) {
            is ObjectPool<T>.ContinuationWaiter -> deliverToContinuationIfActive(waiter, value)
            is ObjectPool<T>.CallbackWaiter -> deliverToCallback(waiter, value)
        }

        /**
         * Drive the delivery loop: repeatedly process the current waiter until the value
         * is handed off or stored back into the pool.
         *
         * The loop body is a single call to [stepDeliver], which returns either:
         * - true (delivery finished), or
         * - false (need to continue with the next waiter returned by [dequeueWaiterOrStore]).
         */
        private fun resumeWaiterOrFallback(initialWaiter: Waiter, value: T) = scope.launch {
            var waiter: Waiter? = initialWaiter

            while (waiter != null) {
                if (stepDeliver(waiter, value)) {
                    return@launch // delivered; done
                }
                // Not delivered yet: fetch next waiter or store back (inside lock).
                waiter = dequeueWaiterOrStore(value) // null => stored back; loop ends
            }
            // waiter == null -> value was stored back by dequeueWaiterOrStore; nothing else to do.
        }

        /**
         * Attempts to deliver [value] to a continuation waiter.
         *
         * @return true if delivered (the continuation was active and has been resumed),
         *         false if the continuation was already cancelled and should be skipped.
         */
        private fun deliverToContinuationIfActive(waiter: ContinuationWaiter, value: T): Boolean {
            val continuation = waiter.continuation
            if (continuation.context.job.isCancelled) {
                return false
            }

            return try {
                continuation.resume(BorrowedObject(value))
                true
            } catch (throwable: Throwable) {
                val isCancellation = throwable is CancellationException || (throwable.cause is CancellationException)
                if (isCancellation) {
                    return false
                }

                val delivered = runCatching { continuation.resumeWithException(throwable) }.isSuccess
                if (!delivered) {
                    throwable.printStackTrace()
                }
                false
            }
        }

        /**
         * Delivers [value] to a callback waiter.
         * Any exception thrown by the callback is confined to this coroutine.
         *
         * @return always true (callback invoked => delivery considered complete).
         */
        private fun deliverToCallback(waiter: CallbackWaiter, value: T): Boolean {
            waiter.callback(Result.success(BorrowedObject(value)))
            return true
        }

        /**
         * Atomically either:
         * - dequeue a waiter (to receive [value]), or
         * - if there is no waiter, store [value] back into [objects].
         *
         * @return the dequeued waiter to be resumed, or null if [value] was stored.
         */
        private fun dequeueWaiterOrStore(value: T): Waiter? = lock.withLock {
            if (waiters.isNotEmpty()) {
                waiters.removeFirst()
            } else {
                objects.addLast(value)
                null
            }
        }
    }

    /**
     * Non-blocking attempt to acquire an object from the pool.
     *
     * @return a [BorrowedObject] if any object is currently available, or `null` otherwise.
     */
    fun acquire(): BorrowedObject? = lock.withLock {
        objects.removeLastOrNull()?.let(::BorrowedObject)
    }

    /**
     * Suspends until an object becomes available, then returns it as a [BorrowedObject].
     * If an object is immediately available, returns without suspension.
     *
     * The decision “return immediately or enqueue as a waiter” is made **atomically**
     * under the same lock to avoid lost wake-ups.
     */
    suspend fun acquireAsync(): BorrowedObject = suspendCancellableCoroutine { continuation ->
        val value = lock.withLock {
            if (objects.isNotEmpty()) {
                BorrowedObject(objects.removeLast())
            } else {
                waiters.addLast(ContinuationWaiter(continuation))
                null
            }
        }
        if (value != null) {
            continuation.resume(value)
            return@suspendCancellableCoroutine
        }

        continuation.invokeOnCancellation {
            removeContinuationIfQueued(continuation)
        }
    }

    private fun removeContinuationIfQueued(continuation: Continuation<BorrowedObject>) = lock.withLock {
        val index = waiters.indexOfLast { waiter -> (waiter as? ObjectPool<T>.ContinuationWaiter)?.continuation === continuation }
        if (index >= 0) {
            waiters.removeAt(index)
        }
    }

    /**
     * Callback-style acquisition that always posts the completion onto [dispatcher].
     * If an object is available immediately, the callback is still invoked asynchronously.
     *
     * @param handler completion callback that receives either a [BorrowedObject] or an error via [Result].
     */
    fun acquireAsync(handler: (Result<ObjectPool<T>.BorrowedObject>) -> Unit) {
        val value = lock.withLock {
            if (objects.isNotEmpty()) {
                objects.removeLast()
            } else {
                waiters.addLast(CallbackWaiter(handler))
                null
            }
        }
        if (value == null) {
            return
        }

        val borrowed = BorrowedObject(value)
        scope.launch { handler(Result.success(borrowed)) }
    }

    fun acquireOrCreate(maker: () -> T): BorrowedObject {
        acquire()?.let { return it }

        val made = maker()
        return BorrowedObject(made)
    }

    suspend fun acquireOrCreateAsync(maker: suspend () -> T): BorrowedObject {
        acquire()?.let { return it }

        val made = maker()
        return BorrowedObject(made)
    }

    fun acquireOrCreateAsync(maker: () -> T, handler: (Result<BorrowedObject>) -> Unit) {
        acquire()?.let { borrowed ->
            scope.launch { handler(Result.success(borrowed)) }
            return
        }
        scope.launch {
            runCatching { BorrowedObject(maker()) }
                .also { result -> handler(result) }
        }
    }

    /**
     * Currently available objects in the pool.
     */
    fun availableCount(): Int = lock.withLock { objects.size }

    /**
     * Currently queued waiters.
     */
    fun waitingCount(): Int = lock.withLock { waiters.size }
}