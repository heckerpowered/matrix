/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.spring

import heckerpowered.foundation.ui.animation.core.AnimationSpec

data class SpringSpec(
    val stiffness: Double,
    val dampingRatio: Double,
    val allowsOvershoot: Boolean = true,
) : AnimationSpec