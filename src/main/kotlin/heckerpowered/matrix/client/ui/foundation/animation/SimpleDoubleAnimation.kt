/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.foundation.animation

import heckerpowered.matrix.client.animationDuration
import java.time.Duration

class SimpleDoubleAnimation(
    from: Double = .0,
    to: Double = .0,
    duration: Duration = animationDuration,
    startTime: Duration = Duration.ZERO,
    easingFunction: EasingFunction = heckerpowered.matrix.client.easingFunction,
    initValue: Double = .0,
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

    var startTime
        get() = animationClock.startTime
        set(value) {
            animationClock.startTime = value
        }

    var animatedValue: Double
        get() = doubleAnimation.animatedValue
        set(value) {
            if (value.isNaN()) {
                return
            }
            doubleAnimation.currentValue = value
            from = value
            to = value
        }

    val progress: Double
        get() = doubleAnimation.progress

    val isAnimating: Boolean
        get() = doubleAnimation.isAnimating

    init {
        animatedValue = initValue
    }

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