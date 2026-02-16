/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.interpolate.builtin

import heckerpowered.foundation.ui.animation.interpolate.Interpolatable

object DoubleInterpolator : Interpolatable<Double> {
    override fun interpolate(from: Double, to: Double, t: Double): Double {
        return from + (to - from) * t
    }
}