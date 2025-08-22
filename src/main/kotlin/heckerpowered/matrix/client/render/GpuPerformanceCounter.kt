package heckerpowered.matrix.client.render

import heckerpowered.matrix.core.common.pool.ObjectPool
import org.lwjgl.opengl.GL46.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

class GpuPerformanceCounter(private val queryObject: Int) {
    companion object {
        val performanceCounters = ObjectPool<GpuPerformanceCounter>()

        @OptIn(ExperimentalContracts::class)
        fun measure(operation: () -> Unit): QueryResult {
            contract {
                callsInPlace(operation, InvocationKind.EXACTLY_ONCE)
            }

            val performanceCounter = performanceCounters.acquireOrCreate { GpuPerformanceCounter(glGenQueries()) }
            performanceCounter.value.beginQuery()
            return try {
                operation()
                QueryResult(performanceCounter)
            } finally {
                performanceCounter.value.endQuery()
            }
        }
    }

    class QueryResult(private val performanceCounter: ObjectPool<GpuPerformanceCounter>.BorrowedObject) {
        private var cachedMeasureTime: Duration? = null

        val measureTime: Duration?
            get() {
                cachedMeasureTime?.let { return it }
                val queryObject = performanceCounter.value.queryObject
                if (glGetQueryObjecti(queryObject, GL_QUERY_RESULT_AVAILABLE) == 0) {
                    return null
                }
                cachedMeasureTime = glGetQueryObjecti64(queryObject, GL_QUERY_RESULT).nanoseconds
                performanceCounter.close()
                return cachedMeasureTime
            }
    }

    private fun beginQuery() {
        glBeginQuery(GL_TIME_ELAPSED, queryObject)
    }

    private fun endQuery() {
        glEndQuery(GL_TIME_ELAPSED)
    }
}