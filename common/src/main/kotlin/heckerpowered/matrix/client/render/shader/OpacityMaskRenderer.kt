/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object OpacityMaskRenderer {
    private val opacityMaskShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/opacity_mask.fsh", GL_FRAGMENT_SHADER),
        )
    }

    fun render(opacityMaskFramebuffer: Framebuffer, colorFramebuffer: Framebuffer) {
        PostProcessRenderer.renderShaderToFramebuffer(
            opacityMaskShader,
            PostProcessRenderer.currentFramebuffer(),
            mapOf("opacityMask" to opacityMaskFramebuffer, "colorAttachment" to colorFramebuffer),
        )
        PostProcessRenderer.nextFramebuffer()
    }
}

infix fun Framebuffer.opacityMask(colorFramebuffer: Framebuffer) {
    OpacityMaskRenderer.render(this, colorFramebuffer)
}
