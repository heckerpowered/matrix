/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import heckerpowered.matrix.client.event.PostProcessCallback
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.UniformProvider
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.SimpleFramebuffer

val framebufferProvider: UniformProvider
    get() = PostProcessRenderer.framebufferProvider

object PostProcessRenderer {
    val postProcessShaders = mutableSetOf<BlitProgram>()

    var sourceFramebuffer: Framebuffer = createFramebuffer()
    private var boundFramebuffer: Framebuffer = sourceFramebuffer
    val framebufferProvider = UniformProvider("framebuffer")
    var useDepthAttachment = false
    var levelOfDetail = .0F

    private val managedFramebuffers = mutableListOf<Framebuffer>()
    private val framebuffers = mutableListOf(createFramebuffer(), createFramebuffer())
    private var currentFramebufferIndex = 0

    fun currentFramebuffer(): Framebuffer {
        return framebuffers[currentFramebufferIndex]
    }

    val ping: Framebuffer
        get() = framebuffers[0]

    val pong: Framebuffer
        get() = framebuffers[1]

    fun nextFramebuffer() {
        currentFramebufferIndex++
        if (currentFramebufferIndex >= framebuffers.size) {
            currentFramebufferIndex = 0
        }
    }

    private fun createFramebuffer(): Framebuffer {
        return SimpleFramebuffer(1, 1, true, false)
    }

    fun createManagedFramebuffer(): Framebuffer {
        val framebuffer = SimpleFramebuffer(1, 1, true, false)
        framebuffer.setClearColor(.0F, .0F, .0F, .0F)
        managedFramebuffers.add(framebuffer)
        return framebuffer
    }

    fun manageFramebuffer(framebuffer: Framebuffer) {
        managedFramebuffers.add(framebuffer)
    }

    @JvmStatic
    fun onResize(width: Int, height: Int) {
        for (framebuffer in framebuffers) {
            framebuffer.resize(width, height, false)
        }
        for (framebuffer in managedFramebuffers) {
            framebuffer.resize(width, height, false)
        }
    }

    @JvmStatic
    fun renderToScreen() {
        if (postProcessShaders.isEmpty()) {
            return
        }

        val renderedFramebuffer = renderPostProcessEffects()
        renderFramebufferToScreen(renderedFramebuffer)
    }

    fun resetFramebuffers() {
        currentFramebufferIndex = 0
    }

    fun clearFramebuffers() {
    }

    @JvmStatic
    fun renderToFramebuffer(framebuffer: Framebuffer) {
        if (postProcessShaders.isEmpty()) {
            return
        }

        val renderedFramebuffer = renderPostProcessEffects()
        copyFramebuffer(renderedFramebuffer, framebuffer)
    }

    @JvmStatic
    fun renderPostProcessEffects(): Framebuffer {
        return renderShaders(postProcessShaders)
    }

    @JvmStatic
    fun renderToMinecraftFramebuffer() {
        PostProcessCallback.EVENT.invoker().onPostProcess()
    }

    @JvmStatic
    fun renderFramebufferToScreen(framebuffer: Framebuffer, disableBlend: Boolean = false) {
    }

    @JvmStatic
    fun renderShaderToFramebuffer(shader: BlitProgram, framebuffer: Framebuffer, disableBlend: Boolean = true) {
        boundFramebuffer = framebuffer
    }

    @JvmStatic
    fun renderShaders(shaders: Collection<BlitProgram>): Framebuffer {
        resetFramebuffers()
        boundFramebuffer = currentFramebuffer()
        for (shader in shaders) {
            nextFramebuffer()
            boundFramebuffer = currentFramebuffer()
        }
        return boundFramebuffer
    }

    @JvmStatic
    fun renderShadersToFramebuffer(shaders: Collection<BlitProgram>, framebuffer: Framebuffer) {
        val renderedFramebuffer = renderShaders(shaders)
        copyFramebuffer(renderedFramebuffer, framebuffer)
    }

    @JvmStatic
    fun copyFramebuffer(from: Framebuffer, to: Framebuffer, disableBlend: Boolean = true, copyDepth: Boolean = false) {
        boundFramebuffer = from
    }

    fun useFramebuffer(framebuffer: Framebuffer, action: () -> Unit) {
        val previousFramebuffer = sourceFramebuffer
        sourceFramebuffer = framebuffer
        boundFramebuffer = currentFramebuffer()
        action()
        sourceFramebuffer = previousFramebuffer
    }
}
