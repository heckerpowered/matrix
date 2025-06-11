package heckerpowered.matrix.core.utility

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

class FixedRateRepeater(
    duration: Duration,
    repeats: Long,
) {
    var duration: Duration = duration
        set(value) {
            field = value
            intervalNanos = recalculateInterval()
        }
    var repeats: Long = repeats
        set(value) {
            require(repeats > 0)
            field = value
            intervalNanos = recalculateInterval()
        }
    var paused: Boolean = false

    private fun recalculateInterval(): Long {
        return duration.toLong(DurationUnit.NANOSECONDS) / repeats
    }

    private var intervalNanos: Long = recalculateInterval()
    private var elapsedTimeNanos = 0L

    val pendingOperations: Long
        get() = elapsedTimeNanos / intervalNanos

    fun update(deltaTime: Duration = 50.milliseconds) {
        if (paused) {
            return
        }
        elapsedTimeNanos += deltaTime.toLong(DurationUnit.NANOSECONDS)
    }

    fun getPendingOperationsIn(duration: Duration = this.duration, repeats: Long = this.repeats): Long {
        require(repeats > 0)

        val interval = duration.toLong(DurationUnit.NANOSECONDS) / repeats
        return elapsedTimeNanos / interval
    }

    fun consumePendingOperations() {
        elapsedTimeNanos = (elapsedTimeNanos % intervalNanos)
    }
}