/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.shader.BlitProgram

object TentShader {
    var framebufferObject = 0
    var levelOfDetail = 0.0F
    val tentBlurShader = BlitProgram()
}
