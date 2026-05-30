/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.color

@JvmInline
value class Argb8(val packed: Int) {
    val alpha: Int get() = (packed ushr 24) and 0xFF
    val red: Int get() = (packed ushr 16) and 0xFF
    val green: Int get() = (packed ushr 8) and 0xFF
    val blue: Int get() = packed and 0xFF

    companion object {
        fun of(a: Int, r: Int, g: Int, b: Int): Argb8 {
            return Argb8(
                (a shl 24) or
                        (r shl 16) or
                        (g shl 8) or
                        b
            )
        }

        fun rgb(r: Int, g: Int, b: Int): Argb8 {
            return of(255, r, g, b)
        }

        fun gray(v: Int): Argb8 {
            return rgb(v, v, v)
        }
    }

    fun withAlpha(a: Int): Argb8 {
        return Argb8((a shl 24) or (packed and 0x00FFFFFF))
    }
}
