/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render

data class Color(
    var red: Int,
    var green: Int,
    var blue: Int,
    var alpha: Int
) {
    fun toInt(): Int {
        return alpha shl 24 or (red shl 16) or (green shl 8) or blue
    }
}
