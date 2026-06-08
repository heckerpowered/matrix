/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.tentBlurShader
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object BloomEffect {
    var brightnessThreshold = 1.0F
    var bloomIntensity = 1.0F
    var brightnessPassFramebuffer: Framebuffer = PostProcessRenderer.sourceFramebuffer
    private val brightFramebuffer: Framebuffer = PostProcessRenderer.createManagedFramebuffer()
    val bloomDownFramebuffer: Framebuffer = PostProcessRenderer.createManagedFramebuffer()
    val bloomUpFramebuffer: Framebuffer = PostProcessRenderer.createManagedFramebuffer()

    private val brightnessShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/bloom/bloom_brightness_pass.fsh", GL_FRAGMENT_SHADER),
        )
    }

    fun renderBloom() {
        val halfFramebuffer = ScaleSampling.getDownScalingFramebuffer(0.5)
        val quarterFramebuffer = ScaleSampling.getDownScalingFramebuffer(0.25)

        PostProcessRenderer.renderShaderToFramebuffer(brightnessShader, brightnessPassFramebuffer, brightFramebuffer)
        ScaleSampling.sample(brightFramebuffer, halfFramebuffer, ScaleSampling.bilinearSample)
        ScaleSampling.sample(halfFramebuffer, quarterFramebuffer, tentBlurShader)
        ScaleSampling.sample(quarterFramebuffer, halfFramebuffer, tentBlurShader)
        ScaleSampling.sample(halfFramebuffer, bloomUpFramebuffer, tentBlurShader)
        PostProcessRenderer.copyFramebuffer(bloomUpFramebuffer, bloomDownFramebuffer)
    }
}
