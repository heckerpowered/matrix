/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.foundation.animation

abstract class EasingFunction {
    var easingMode = EasingMode.IN

    fun transform(normalizedTime: Double): Double {
        return when (easingMode) {
            EasingMode.IN -> transformCore(normalizedTime)
            EasingMode.OUT ->
                // Ease out is the same as ease in, except time is reversed & the result is
                // flipped.
                1.0 - transformCore(1.0 - normalizedTime)

            EasingMode.BOTH ->
                // Ease in&out is a combination of EaseIn & EaseOut fit to the 0-1, 0-1 range.
                if ((normalizedTime < 0.5)) transformCore(normalizedTime * 2.0) * 0.5
                else (1.0 - transformCore((1.0 - normalizedTime) * 2.0)) * 0.5 + 0.5
        }
    }

    protected abstract fun transformCore(normalizedTime: Double): Double
}