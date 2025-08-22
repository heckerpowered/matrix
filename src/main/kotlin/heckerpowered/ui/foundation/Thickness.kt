/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.ui.foundation

/**
 * Represents the thickness of an element (e.g. padding, margin, border).
 */
data class Thickness(
    /**
     * The thickness of the top side of the element.
     */
    var bottom: Double,

    /**
     * The thickness of the left side of the element.
     */
    var left: Double,

    /**
     * The thickness of the right side of the element.
     */
    var right: Double,

    /**
     * The thickness of the bottom side of the element.
     */
    var top: Double
) {
    /**
     * Creates a new [Thickness] with the same length on all sides.
     */
    constructor(uniformLength: Double) : this(uniformLength, uniformLength, uniformLength, uniformLength)
}
