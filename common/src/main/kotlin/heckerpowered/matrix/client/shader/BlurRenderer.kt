/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.post.ScaleSampling
import heckerpowered.matrix.client.render.shader.GaussianBlurRenderer
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector2f
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object BlurRenderer {
    var initialFramebuffer: Framebuffer = PostProcessRenderer.sourceFramebuffer
    var currentFramebuffer: Framebuffer = PostProcessRenderer.sourceFramebuffer

    var radius = 5.0F
    var kawaseOffset = 1.0F
    var useDownscaling: Boolean = true

    val blurFramebuffer by lazy {
        PostProcessRenderer.createManagedFramebuffer()
    }

    val horizontalBlurShader = GaussianBlurRenderer.gaussianBlurShader
    val verticalBlurShader = GaussianBlurRenderer.gaussianBlurShader
    val kawaseBlurShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/blur/kawase_blur.fsh", GL_FRAGMENT_SHADER),
    )
    val tentBlurShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/blur/tent.fsh", GL_FRAGMENT_SHADER),
    )
    val blurTextureRenderProgram = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/blur/blur_mask.fsh", GL_FRAGMENT_SHADER),
    )

    fun dumpFrameBuffer(framebuffer: Framebuffer) {
    }

    fun renderQuad() {
    }

    fun renderGaussianBlurFullResolution(source: Framebuffer = PostProcessRenderer.sourceFramebuffer) {
        blurFramebuffer.clear(false)
        GaussianBlurRenderer.fullPing.clear(false)
        GaussianBlurRenderer.fullPong.clear(false)

        GaussianBlurRenderer.direction = Vector2f(1F, 0F)
        PostProcessRenderer.renderShaderToFramebuffer(GaussianBlurRenderer.gaussianBlurShader, source, GaussianBlurRenderer.fullPing)

        GaussianBlurRenderer.direction = Vector2f(0F, 1F)
        PostProcessRenderer.renderShaderToFramebuffer(GaussianBlurRenderer.gaussianBlurShader, GaussianBlurRenderer.fullPing, GaussianBlurRenderer.fullPong)

        PostProcessRenderer.copyFramebuffer(GaussianBlurRenderer.fullPong, blurFramebuffer)
    }

    fun renderGaussianBlur(source: Framebuffer = PostProcessRenderer.sourceFramebuffer, target: Framebuffer = blurFramebuffer) {
        val halfFramebuffer = ScaleSampling.getDownScalingFramebuffer(0.5)
        val quarterFramebuffer = ScaleSampling.getDownScalingFramebuffer(0.25)

        target.clear(false)
        GaussianBlurRenderer.ping.clear(false)
        GaussianBlurRenderer.pong.clear(false)

        ScaleSampling.sample(source, halfFramebuffer, ScaleSampling.bilinearSample)
        ScaleSampling.sample(halfFramebuffer, quarterFramebuffer, ScaleSampling.bilinearSample)

        GaussianBlurRenderer.direction = Vector2f(1F, 0F)
        PostProcessRenderer.renderShaderToFramebuffer(GaussianBlurRenderer.gaussianBlurShader, quarterFramebuffer, GaussianBlurRenderer.ping)

        GaussianBlurRenderer.direction = Vector2f(0F, 1F)
        PostProcessRenderer.renderShaderToFramebuffer(GaussianBlurRenderer.gaussianBlurShader, GaussianBlurRenderer.ping, GaussianBlurRenderer.pong)

        ScaleSampling.sample(GaussianBlurRenderer.pong, halfFramebuffer, ScaleSampling.bilinearSample)
        ScaleSampling.sample(halfFramebuffer, target, ScaleSampling.bilinearSample)
    }

    fun renderKawaseBlur() {
        val halfFramebuffer = ScaleSampling.getDownScalingFramebuffer(0.5)
        val quarterFramebuffer = ScaleSampling.getDownScalingFramebuffer(0.25)
        ScaleSampling.sample(PostProcessRenderer.sourceFramebuffer, halfFramebuffer, ScaleSampling.bilinearSample)
        ScaleSampling.sample(halfFramebuffer, quarterFramebuffer, ScaleSampling.bilinearSample)
        kawaseOffset = .0F
        PostProcessRenderer.useFramebuffer(quarterFramebuffer) {
            PostProcessRenderer.renderShadersToFramebuffer(List(5) { kawaseBlurShader }, blurFramebuffer)
        }
    }

    fun renderBlur() {
        renderGaussianBlur()
        // renderKawaseBlur()
    }

    @JvmStatic
    fun onResize(width: Int, height: Int) {
        blurFramebuffer.resize(width, height, false)
    }
}
