/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.foundation.animation

data class ColorAnimation(
    val red: SimpleDoubleAnimation = SimpleDoubleAnimation(),
    val green: SimpleDoubleAnimation = SimpleDoubleAnimation(),
    val blue: SimpleDoubleAnimation = SimpleDoubleAnimation(),
)