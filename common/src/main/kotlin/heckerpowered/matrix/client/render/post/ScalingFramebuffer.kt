/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import net.minecraft.client.gl.SimpleFramebuffer
import kotlin.math.roundToInt

class ScalingFramebuffer(
    private val scaling: Double,
    width: Int = 1,
    height: Int = 1,
    useDepth: Boolean = false,
    getError: Boolean = false,
) : SimpleFramebuffer(scaledSize(width, scaling), scaledSize(height, scaling), useDepth, getError) {
    override fun resize(width: Int, height: Int, getError: Boolean) {
        super.resize(scaledSize(width, scaling), scaledSize(height, scaling), getError)
    }
}

private fun scaledSize(size: Int, scaling: Double): Int {
    return (size * scaling).roundToInt().coerceAtLeast(1)
}
