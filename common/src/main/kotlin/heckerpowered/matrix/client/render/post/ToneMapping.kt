/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object ToneMapping {
    var exposureLinear = 1.0F
    var exposureEv = 0.0F
    val toneMapFramebuffer: Framebuffer = PostProcessRenderer.createManagedFramebuffer()

    private val toneMappingProgram by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/tone_mapping/aces_filmic.fsh", GL_FRAGMENT_SHADER),
        )
    }

    fun render(sourceFramebuffer: Framebuffer, targetFramebuffer: Framebuffer) {
        PostProcessRenderer.renderShaderToFramebuffer(
            toneMappingProgram,
            targetFramebuffer,
            mapOf("hdrScene" to sourceFramebuffer),
        )
    }
}
