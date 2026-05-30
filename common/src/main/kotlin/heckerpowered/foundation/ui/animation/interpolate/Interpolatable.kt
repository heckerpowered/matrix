/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.interpolate

interface Interpolatable<T> {
    fun interpolate(from: T, to: T, t: Double): T
}