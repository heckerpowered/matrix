package heckerpowered.matrix.client.ui.foundation.animation

import heckerpowered.matrix.client.animationDuration
import java.time.Duration

class SimpleDoubleAnimation(
    from: Double = .0,
    to: Double = .0,
    duration: Duration = animationDuration,
    easingFunction: EasingFunction = heckerpowered.matrix.client.easingFunction
) {
    private val animationClock = AnimationClock(duration, from, to)
    private val doubleAnimation = DoubleAnimation(animationClock, easingFunction)

    var from
        get() = animationClock.from
        set(value) {
            animationClock.from = value
        }

    var to
        get() = animationClock.to
        set(value) {
            animationClock.to = value
        }

    var duration
        get() = animationClock.duration
        set(value) {
            animationClock.duration = value
        }

    var value: Double
        get() = doubleAnimation.currentValue
        set(value) {
            doubleAnimation.currentValue = value
        }

    val animatedValue: Double
        get() = doubleAnimation.animatedValue

    fun start() {
        animationClock.start()
    }

    fun stop() {
        animationClock.stop()
    }

    fun resume() {
        animationClock.resume()
    }

    fun suspend() {
        animationClock.suspend()
    }
}