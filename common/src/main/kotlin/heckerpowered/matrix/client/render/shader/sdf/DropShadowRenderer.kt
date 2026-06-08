/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader.sdf

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector2f
import org.joml.Vector4f
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object DropShadowRenderer {
    var shadowOffset = Vector2f()
    var shadowSize = 8.0F
    var shadowColor = Vector4f(.0F, .0F, .0F, .5F)

    private val dropShadowShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/sdf/drop_shadow.fsh", GL_FRAGMENT_SHADER),
    )

    fun render(signedDistanceField: Framebuffer) {
        PostProcessRenderer.renderShaderToFramebuffer(
            dropShadowShader,
            PostProcessRenderer.currentFramebuffer(),
            mapOf("signedDistanceField" to signedDistanceField),
        )
        PostProcessRenderer.nextFramebuffer()
    }
}
