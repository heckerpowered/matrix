/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.ui.foundation.animation

data class ColorAnimation(
    val red: SimpleDoubleAnimation = SimpleDoubleAnimation(),
    val green: SimpleDoubleAnimation = SimpleDoubleAnimation(),
    val blue: SimpleDoubleAnimation = SimpleDoubleAnimation(),
)