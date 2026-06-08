/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram

object VelocityMapRenderer {
    val velocityMap = PostProcessRenderer.createManagedFramebuffer()
    val velocityMapShader = BlitProgram()
}
