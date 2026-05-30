/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.tween

import heckerpowered.foundation.ui.animation.core.Animation
import heckerpowered.foundation.ui.animation.core.AnimationSpec
import heckerpowered.foundation.ui.animation.interpolate.Interpolatable
import heckerpowered.foundation.ui.animation.time.DelayedStart
import heckerpowered.foundation.ui.animation.time.Timeline
import heckerpowered.matrix.client.ui.foundation.animation.EasingFunction

class TweenAnimation<T>(
    initialValue: T,
    private val interpolator: Interpolatable<T>,
    var timeline: Timeline,
    var easingFunction: EasingFunction? = null,
) : Animation<T> {
    private var from: T = initialValue
    private var to: T = initialValue

    override fun value(): T {
        val progress = easingFunction?.transform(timeline.progress()) ?: timeline.progress()
        return interpolator.interpolate(from, to, progress)
    }

    override fun animateTo(value: T, spec: AnimationSpec) {
        val tweenSpec = spec as? TweenSpec ?: return snapTo(value)

        from = value()
        to = value

        timeline.duration = tweenSpec.duration
        easingFunction = tweenSpec.easingFunction
        (timeline as? DelayedStart)?.let { it.delay = tweenSpec.delay }
        timeline.reset()
    }

    override fun snapTo(value: T) {
        from = value
        to = value
    }
}