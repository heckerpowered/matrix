/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.event.PostProcessCallback
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.core.times
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object ShockwaveRenderer {
    var wavePosition: Vector3f = Vector3f()
    var waveColor = Vector4f(0.1F, 0.5F, 1.0F, 1.0F) * 4.0F
    var waveRadius = SimpleDoubleAnimation()
    var waveSize = SimpleDoubleAnimation()

    val shockwaveShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/shockwave.fsh", GL_FRAGMENT_SHADER),
    )

    fun onInitialize() {
        PostProcessCallback.EVENT.register(::render)
    }

    private fun render() {
        if (!isActive()) {
            return
        }

        val source = PostProcessRenderer.sourceFramebuffer
        val output = PostProcessRenderer.currentFramebuffer()
        PostProcessRenderer.renderShaderToFramebuffer(
            shockwaveShader,
            output,
            mapOf("framebuffer" to source, "depthAttachment" to source),
        )
        PostProcessRenderer.copyFramebuffer(output, source)
    }

    private fun isActive(): Boolean {
        return waveRadius.isAnimating ||
            waveSize.isAnimating ||
            waveSize.animatedValue > 0.001
    }
}
