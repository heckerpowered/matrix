/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

data class Color(
    var red: Int,
    var green: Int,
    var blue: Int,
    var alpha: Int,
) {
    fun toInt(): Int {
        return alpha shl 24 or (red shl 16) or (green shl 8) or blue
    }
}
