/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.event.PostProcessCallback
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.state.FramebufferState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.render.state.ViewportState
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.SimpleFramebuffer
import org.lwjgl.opengl.GL31
import org.lwjgl.opengl.GL46

val framebufferProvider: UniformProvider
    get() = PostProcessRenderer.framebufferProvider

object PostProcessRenderer {
    val postProcessShaders = mutableSetOf<BlitProgram>()

    /**
     * The source framebuffer to render the post process effects from.
     */
    var sourceFramebuffer = minecraft.framebuffer
    private var boundFramebuffer = minecraft.framebuffer

    val framebufferProvider = UniformProvider("framebuffer") { pointer ->
        GL31.glActiveTexture(GlConst.GL_TEXTURE0)
        GL31.glBindTexture(GlConst.GL_TEXTURE_2D, boundFramebuffer.colorAttachment)
        RenderSystem.glUniform1i(pointer, 0)
    }

    var useDepthAttachment = false
    private val depthAttachmentProvider = UniformProvider("depthAttachment") { pointer ->
        if (pointer == -1 || !boundFramebuffer.useDepthAttachment || !useDepthAttachment) {
            return@UniformProvider
        }

        GL31.glActiveTexture(GlConst.GL_TEXTURE0 + 1)
        GL31.glBindTexture(GlConst.GL_TEXTURE_2D, boundFramebuffer.depthAttachment)
        RenderSystem.glUniform1i(pointer, 1)
    }

    var levelOfDetail = .0F
    private val levelOfDetailProvider = UniformProvider("lod") { pointer ->
        if (pointer == -1) {
            return@UniformProvider
        }

        GL46.glUniform1f(pointer, levelOfDetail)
    }

    private val blitShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL46.GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/blit/blit.fsh", GL46.GL_FRAGMENT_SHADER),
            uniforms = arrayOf(framebufferProvider, depthAttachmentProvider, levelOfDetailProvider)
        )
    }

    private val blitNoDepthShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL46.GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/blit/blit_no_depth.fsh", GL46.GL_FRAGMENT_SHADER),
            uniforms = arrayOf(framebufferProvider, levelOfDetailProvider)
        )
    }

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
        val framebuffer = SimpleFramebuffer(
            minecraft.window.framebufferWidth,
            minecraft.window.framebufferHeight,
            true,
            MinecraftClient.IS_SYSTEM_MAC
        )
        framebuffer.setClearColor(.0F, .0F, .0F, .0F)
        return framebuffer
    }

    fun createManagedFramebuffer(): Framebuffer {
        val framebuffer = SimpleFramebuffer(
            minecraft.window.framebufferWidth,
            minecraft.window.framebufferHeight,
            true,
            MinecraftClient.IS_SYSTEM_MAC
        )
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
            framebuffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC)
        }
        for (framebuffer in managedFramebuffers) {
            framebuffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC)
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
        StateIsolation.isolate(FramebufferState.captureSnapshot(), ViewportState.captureSnapshot()) {
            currentFramebufferIndex = 0
            framebuffers.forEach { it.clear(MinecraftClient.IS_SYSTEM_MAC) }
        }
    }

    fun clearFramebuffers() {
        StateIsolation.isolate(FramebufferState.captureSnapshot(), ViewportState.captureSnapshot()) {
            framebuffers.forEach { it.clear(MinecraftClient.IS_SYSTEM_MAC) }
        }
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
        renderToFramebuffer(minecraft.framebuffer)
        PostProcessCallback.EVENT.invoker().onPostProcess()
    }

    @JvmStatic
    fun renderFramebufferToScreen(framebuffer: Framebuffer, disableBlend: Boolean = false) {
        val previousFramebuffer = GlStateManager.getBoundFramebuffer()

        framebuffer.endWrite()
        framebuffer.draw(minecraft.window.framebufferWidth, minecraft.window.framebufferHeight, disableBlend)

        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, previousFramebuffer)
    }

    @JvmStatic
    fun renderShaderToFramebuffer(shader: BlitProgram, framebuffer: Framebuffer, disableBlend: Boolean = true) {
        val previousFramebuffer = GlStateManager.getBoundFramebuffer()

        framebuffer.beginWrite(true)
        if (disableBlend) {
            shader.blit()
        } else {
            shader.enableShader()
            BlitProgram.blit()
            shader.disableShader()
        }
        framebuffer.endWrite()

        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, previousFramebuffer)
    }

    @JvmStatic
    fun renderShaders(shaders: Collection<BlitProgram>): Framebuffer {
        val previousFramebuffer = GlStateManager.getBoundFramebuffer()

        resetFramebuffers()
        copyFramebuffer(sourceFramebuffer, currentFramebuffer())
        boundFramebuffer = currentFramebuffer()

        // Render post process effects
        for (shader in shaders) {
            // Render shader to next framebuffer
            nextFramebuffer()
            currentFramebuffer().beginWrite(false)
            shader.blit()

            // Bind the rendered framebuffer
            boundFramebuffer = currentFramebuffer()
        }

        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, previousFramebuffer)
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
        val shader = if (copyDepth) blitShader else blitNoDepthShader
        renderShaderToFramebuffer(shader, to, disableBlend)
    }

    fun useFramebuffer(framebuffer: Framebuffer, action: () -> Unit) {
        val previousFramebuffer = sourceFramebuffer
        sourceFramebuffer = framebuffer

        val previousBoundFramebuffer = GlStateManager.getBoundFramebuffer()
        currentFramebuffer().clear(false)
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, previousBoundFramebuffer)

        copyFramebuffer(sourceFramebuffer, currentFramebuffer())
        boundFramebuffer = currentFramebuffer()

        action()
        sourceFramebuffer = previousFramebuffer
    }
}