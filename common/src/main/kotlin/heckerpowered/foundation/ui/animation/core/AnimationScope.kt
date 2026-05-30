/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.core

import heckerpowered.foundation.ui.animation.interpolate.Interpolatable
import heckerpowered.foundation.ui.animation.interpolate.builtin.DoubleInterpolator
import heckerpowered.foundation.ui.animation.state.AnimatedState
import heckerpowered.foundation.ui.animation.state.MutableState
import heckerpowered.foundation.ui.animation.time.MonotonicTimeline
import heckerpowered.foundation.ui.animation.tween.TweenAnimation
import kotlin.time.Duration

class AnimationScope {
    internal var currentSpec: AnimationSpec? = null

    fun withAnimation(spec: AnimationSpec, block: () -> Unit) {
        currentSpec = spec
        try {
            block()
        } finally {
            currentSpec = null
        }
    }

    fun doubleAnimation(initialValue: Double): MutableState<Double> {
        val animation = TweenAnimation(initialValue, DoubleInterpolator, MonotonicTimeline(Duration.ZERO))
        return AnimatedState(this, animation)
    }

    fun <T> animate(initialValue: T, interpolatable: Interpolatable<T>): MutableState<T> {
        val animation = TweenAnimation(initialValue, interpolatable, MonotonicTimeline(Duration.ZERO))
        return AnimatedState(this, animation)
    }
}