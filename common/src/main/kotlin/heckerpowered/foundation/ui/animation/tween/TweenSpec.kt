/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.tween

import heckerpowered.foundation.ui.animation.core.AnimationSpec
import heckerpowered.matrix.client.ui.foundation.animation.EasingFunction
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class TweenSpec(
    val duration: Duration = 300.milliseconds,
    val easingFunction: EasingFunction? = null,
    val delay: Duration = Duration.ZERO,
) : AnimationSpec