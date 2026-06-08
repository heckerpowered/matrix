/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import heckerpowered.matrix.client.render.PostProcessRenderer
import net.minecraft.client.gl.Framebuffer

object BlurRenderer {
    var initialFramebuffer: Framebuffer = PostProcessRenderer.sourceFramebuffer
    var currentFramebuffer: Framebuffer = PostProcessRenderer.sourceFramebuffer

    var radius = 5.0F
    var kawaseOffset = 1.0F
    var useDownscaling: Boolean = true

    val blurFramebuffer by lazy {
        PostProcessRenderer.createManagedFramebuffer()
    }

    val horizontalBlurShader = BlitProgram()
    val verticalBlurShader = BlitProgram()
    val kawaseBlurShader = BlitProgram()
    val tentBlurShader = BlitProgram()
    val blurTextureRenderProgram = Program()

    fun dumpFrameBuffer(framebuffer: Framebuffer) {
    }

    fun renderQuad() {
    }

    fun renderGaussianBlurFullResolution(source: Framebuffer = PostProcessRenderer.sourceFramebuffer) {
    }

    fun renderGaussianBlur(source: Framebuffer = PostProcessRenderer.sourceFramebuffer, target: Framebuffer = blurFramebuffer) {
    }

    fun renderKawaseBlur() {
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
