/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object TentShader {
    var framebufferObject = 0
    var levelOfDetail = 0.0F
    val tentBlurShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/blur/tent.fsh", GL_FRAGMENT_SHADER),
    )

    fun enable(framebufferObject: Int, levelOfDetail: Float) {
        this.framebufferObject = framebufferObject
        this.levelOfDetail = levelOfDetail
        tentBlurShader.enableShader()
    }

    fun disable() {
        tentBlurShader.disableShader()
    }
}
