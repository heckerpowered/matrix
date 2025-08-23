/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.ui.foundation.animation

import kotlin.math.abs

class DoubleAnimation(
    private val animationClock: AnimationClock,
    private val easingFunction: EasingFunction,
) {
    private var privateCurrentValue: Double = 0.0

    var currentValue: Double
        get() = privateCurrentValue
        set(value) {
            if (value.isNaN() || abs(value - privateCurrentValue) < 0.001) {
                return
            }

            privateCurrentValue = value
            animationClock.from = animatedValue
            animationClock.to = privateCurrentValue
            animationClock.start()
        }

    val animatedValue: Double
        get() = animationClock.transform(easingFunction)

    val isAnimating: Boolean
        get() = abs(animatedValue - currentValue) > 0.001

    val progress: Double
        get() = animationClock.progress
}