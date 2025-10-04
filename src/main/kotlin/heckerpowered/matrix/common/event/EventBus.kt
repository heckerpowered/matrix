/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.event

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * A simple, synchronous event bus with type-safe subscriptions.
 *
 * Features:
 * - Listeners are grouped by the exact event class.
 * - Supertypes of the event class are also dispatched (breadth-first).
 * - Supports cancellable events: if the event implements [CancellableEvent],
 *   listeners may set [CancellableEvent.isCancelled] to true. If [EventPolicy.stopOnCancel]
 *   is true for a listener, delivery of that bucket stops when cancelled.
 *
 * @author heckerpowered
 */
class EventBus {
    /**
     * Handle returned by [subscribe] or [on]. Cancelling removes the listener from the bus.
     */
    class Subscription internal constructor(private val cancelAction: () -> Unit) {
        fun cancel() = cancelAction()
    }

    /**
     * Internal entry sorting a listener and its delivery policy.
     */
    private data class ListenerEntry<T>(val listener: EventListener<T>, val policy: EventPolicy<T>)

    private val listenersByType: MutableMap<KClass<*>, MutableList<ListenerEntry<*>>> = mutableMapOf()

    /**
     * Subscribes a listener for events of type [eventClass].
     *
     * @param eventClass Event class to listen for.
     * @param policy Delivery policy, e.g. whether to stop on cancel.
     * @param listener The listener to invoke when an event of this type (or subtype) is posted.
     * @return [Subscription] that can be cancelled to remove the listener.
     */
    fun <T : Any> subscribe(eventClass: KClass<T>, policy: EventPolicy<T>, listener: EventListener<T>): Subscription {
        val list = listenersByType.getOrPut(eventClass) { CopyOnWriteArrayList() }
        val entry = ListenerEntry(listener, policy)
        list.add(entry)
        return Subscription { list.remove(entry) }
    }

    inline fun <reified E : Any> on(
        policy: EventPolicy<E> = EventPolicy(),
        noinline block: (E) -> Unit,
    ): Subscription = subscribe(E::class, policy, block)

    /**
     * Collects the exact class of [event] and all its supertypes.
     * Used to determine which listener buckets to deliver to.
     */
    private fun <E : Any> collectDispatchTypes(event: E): List<KClass<*>> {
        val exact = event::class
        val result = ArrayList<KClass<*>>(8)
        result.add(exact)

        // If no listener asked for supertypes we still compute; cheap enough, and keeps behavior uniform.
        fun addAllSupertypes(type: KClass<*>) {
            for (supertype in type.supertypes.mapNotNull { it.classifier as? KClass<*> }) {
                result.add(supertype)
                addAllSupertypes(supertype)
            }
        }
        addAllSupertypes(exact)
        return result.distinct()
    }

    /**
     * Posts [event] to all listeners of its type and supertypes.
     *
     * Listeners are invoked synchronously, in the calling thread.
     * Exceptions propagate to the caller.
     *
     * Cancellation: if [event] is a [CancellableEvent], listeners may set
     * [CancellableEvent.isCancelled]. Delivery within a type bucket stops
     * if [EventPolicy.stopOnCancel] is true.
     */
    fun <T : Any> post(event: T) {
        val cancellable = event as? CancellableEvent
        val dispatchTypes = collectDispatchTypes(event)

        for (type in dispatchTypes) {
            @Suppress("UNCHECKED_CAST")
            val entries = (listenersByType[type] ?: continue) as List<ListenerEntry<T>>
            if (deliverBucket(event, entries, cancellable)) {
                // Interrupted this bucket due to cancellation+stopOnCancel; proceed to next type bucket.
                continue
            }
        }
    }

    /**
     * Delivers [event] to all listeners in [entries].
     * @return true if delivery was interrupted due to cancellation + [EventPolicy.stopOnCancel].
     */
    private fun <T : Any> deliverBucket(event: T, entries: List<ListenerEntry<T>>, cancellable: CancellableEvent?): Boolean {
        var isCancelled = cancellable?.isCancelled == true
        for (entry in entries) {
            if (isCancelled && entry.policy.stopOnCancel) {
                return true
            }

            entry.listener.onEvent(event)
            isCancelled = cancellable?.isCancelled == true
        }
        return false
    }
}