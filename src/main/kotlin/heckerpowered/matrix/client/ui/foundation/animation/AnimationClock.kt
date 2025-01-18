package heckerpowered.matrix.client.ui.foundation.animation

import heckerpowered.matrix.core.lerp
import net.minecraft.util.math.MathHelper
import org.apache.commons.lang3.time.StopWatch
import java.time.Duration

class AnimationClock(var duration: Duration, var from: Double, var to: Double) {
    private val stopwatch = StopWatch.create()

    fun start() {
        if (stopwatch.isStarted) {
            stopwatch.reset()
        }
        stopwatch.start()
    }

    fun stop() {
        if (stopwatch.isStarted || stopwatch.isSuspended) {
            stopwatch.stop()
        }
    }

    fun suspend() {
        if (stopwatch.isStarted) {
            stopwatch.suspend()
        }
    }

    fun resume() {
        if (stopwatch.isSuspended) {
            stopwatch.resume()
        }
    }

    fun getValue(): Double {
        return getValueAt(stopwatch.nanoTime)
    }

    fun transform(easingFunction: EasingFunction): Double {
        return lerp(easingFunction.transform(getValue()), from, to)
    }

    private fun getValueAt(timeNanos: Long): Double {
        val progress = (timeNanos.toDouble() / duration.toNanos().toDouble()).coerceIn(.0..1.0)
        return MathHelper.lerp(progress, 0.0, 1.0)
    }
}