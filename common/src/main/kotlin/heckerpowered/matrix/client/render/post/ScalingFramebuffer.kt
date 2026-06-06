/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import net.minecraft.client.gl.SimpleFramebuffer

class ScalingFramebuffer(
    private val scaling: Double,
    width: Int = 1,
    height: Int = 1,
    useDepth: Boolean = false,
    getError: Boolean = false,
) : SimpleFramebuffer(width, height, useDepth, getError)
