/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object TexturePixelDissolveProgram : BlitProgram(
    ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
    ResourceShader("/assets/matrix/shaders/post/dissolve/texture_pixel_dissolve.fsh", GL_FRAGMENT_SHADER),
) {
    var noiseTextureId: Int = 0
    var normalTextureId: Int = 0
    var dissolveFactor: Float = 0.0F
}
