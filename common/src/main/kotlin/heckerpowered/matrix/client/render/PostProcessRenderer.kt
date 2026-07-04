/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.event.PostProcessCallback
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.FramebufferExtension
import org.joml.Vector4f

val framebufferProvider: TextureProvider
    get() = PostProcessRenderer.framebufferProvider

object PostProcessRenderer {
    val postProcessShaders = mutableSetOf<BlitProgram>()

    /**
     * The source framebuffer to render the post process effects from.
     * Defaults to the main render target; resolved lazily because the game renderer
     * does not exist yet while mods are being initialized.
     */
    var sourceFramebuffer: RenderTarget
        get() = sourceFramebufferOverride ?: minecraft.mainRenderTarget
        set(value) {
            sourceFramebufferOverride = value
        }
    private var sourceFramebufferOverride: RenderTarget? = null

    private var boundFramebuffer: RenderTarget
        get() = boundFramebufferOverride ?: minecraft.mainRenderTarget
        set(value) {
            boundFramebufferOverride = value
        }
    private var boundFramebufferOverride: RenderTarget? = null

    /**
     * The previous pass' output, exposed to every post shader through the `framebuffer` sampler.
     * Replaces the former glActiveTexture/glBindTexture uniform provider.
     */
    val framebufferProvider = TextureProvider("framebuffer", bilinear = false, mipmap = false) {
        boundFramebuffer.colorTextureView
    }

    var useDepthAttachment = false
    private val depthAttachmentProvider = TextureProvider("depthAttachment") {
        if (boundFramebuffer.useDepth && useDepthAttachment) boundFramebuffer.depthTextureView else null
    }

    var levelOfDetail = .0F
    private val levelOfDetailProvider = UniformProvider("BlitConfig") {
        putFloat(levelOfDetail)
    }

    private val blitShader by lazy {
        BlitProgram(
            "blit/blit.fsh",
            uniforms = arrayOf(levelOfDetailProvider),
            textures = arrayOf(framebufferProvider, depthAttachmentProvider),
            writesDepth = true
        )
    }

    private val blitNoDepthShader by lazy {
        BlitProgram(
            "blit/blit_no_depth.fsh",
            uniforms = arrayOf(levelOfDetailProvider),
            textures = arrayOf(framebufferProvider)
        )
    }

    /**
     * The 1.21 vanilla `Framebuffer.draw` equivalent: samples and blends every pixel, black
     * included — no discard. The HUD composite steps that used vanilla draw pre-migration
     * (hud-over-backdrop, blur-layer-to-main) must NOT go through the discarding blit, or
     * pure-black panel fills and shadow rgb silently vanish from the composite.
     */
    private val blitDrawShader by lazy {
        BlitProgram(
            "blit/blit_replace.fsh",
            uniforms = arrayOf(levelOfDetailProvider),
            textures = arrayOf(framebufferProvider)
        )
    }

    /** Draws [from] over [to] like 1.21's vanilla Framebuffer.draw: blended, never discarding. */
    @JvmStatic
    fun drawFramebuffer(from: RenderTarget, to: RenderTarget, blend: BlendFunction?) {
        val previousBound = boundFramebuffer
        boundFramebuffer = from
        blitDrawShader.drawTo(to, blend)
        boundFramebuffer = previousBound
    }

    private val managedFramebuffers = mutableListOf<RenderTarget>()
    private val framebuffers by lazy {
        mutableListOf(createFramebuffer("matrix post ping"), createFramebuffer("matrix post pong"))
    }
    private var currentFramebufferIndex = 0

    fun currentFramebuffer(): RenderTarget {
        return framebuffers[currentFramebufferIndex]
    }

    val ping: RenderTarget
        get() = framebuffers[0]

    val pong: RenderTarget
        get() = framebuffers[1]

    fun nextFramebuffer() {
        currentFramebufferIndex++
        if (currentFramebufferIndex >= framebuffers.size) {
            currentFramebufferIndex = 0
        }
    }

    private fun createFramebuffer(label: String): RenderTarget {
        // 26.2 release: the color format moved into the constructor; the pre-migration
        // buffers were HDR through the framebuffer mixin's format override, so pass the
        // same format explicitly here.
        return TextureTarget(
            label,
            minecraft.window.width,
            minecraft.window.height,
            true,
            FramebufferExtension.framebufferColorFormat
        )
    }

    fun createManagedFramebuffer(): RenderTarget {
        // Managed framebuffers capture VANILLA rendering (entities/GUI via the output
        // overrides); 26.2 release validates attachment formats against the vanilla
        // pipelines' declared RGBA8 output, so these cannot be HDR.
        val framebuffer = TextureTarget(
            "matrix post managed",
            minecraft.window.width,
            minecraft.window.height,
            true,
            GpuFormat.RGBA8_UNORM
        )
        managedFramebuffers.add(framebuffer)
        return framebuffer
    }

    fun manageFramebuffer(framebuffer: RenderTarget) {
        managedFramebuffers.add(framebuffer)
    }

    @JvmStatic
    fun onResize(width: Int, height: Int) {
        for (framebuffer in framebuffers) {
            framebuffer.resize(width, height)
        }
        for (framebuffer in managedFramebuffers) {
            framebuffer.resize(width, height)
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

    /** Clears a framebuffer to transparent black (the previous setClearColor(0,0,0,0) semantics). */
    @JvmStatic
    fun clear(framebuffer: RenderTarget) {
        val encoder = RenderSystem.getDevice().createCommandEncoder()
        val color = framebuffer.colorTexture ?: return
        val depth = framebuffer.depthTexture
        val transparentBlack = Vector4f(0F, 0F, 0F, 0F)
        if (framebuffer.useDepth && depth != null) {
            encoder.clearColorAndDepthTextures(color, transparentBlack, depth, 1.0)
        } else {
            encoder.clearColorTexture(color, transparentBlack)
        }
    }

    fun resetFramebuffers() {
        currentFramebufferIndex = 0
        framebuffers.forEach(::clear)
    }

    fun clearFramebuffers() {
        framebuffers.forEach(::clear)
    }

    @JvmStatic
    fun renderToFramebuffer(framebuffer: RenderTarget) {
        if (postProcessShaders.isEmpty()) {
            return
        }

        val renderedFramebuffer = renderPostProcessEffects()
        copyFramebuffer(renderedFramebuffer, framebuffer)
    }

    @JvmStatic
    fun renderPostProcessEffects(): RenderTarget {
        return renderShaders(postProcessShaders)
    }

    @JvmStatic
    fun renderToMinecraftFramebuffer() {
        renderToFramebuffer(minecraft.mainRenderTarget)
        PostProcessCallback.EVENT.invoker().onPostProcess()
    }

    @JvmStatic
    @JvmOverloads
    fun renderFramebufferToScreen(framebuffer: RenderTarget, disableBlend: Boolean = false) {
        boundFramebuffer = framebuffer
        blitNoDepthShader.drawTo(
            minecraft.mainRenderTarget,
            blend = if (disableBlend) null else BlendFunction.TRANSLUCENT
        )
    }

    @JvmStatic
    @JvmOverloads
    fun renderShaderToFramebuffer(shader: BlitProgram, framebuffer: RenderTarget, blend: BlendFunction? = null) {
        shader.drawTo(framebuffer, blend)
    }

    @JvmStatic
    fun renderShaders(shaders: Collection<BlitProgram>): RenderTarget {
        resetFramebuffers()
        copyFramebuffer(sourceFramebuffer, currentFramebuffer())
        boundFramebuffer = currentFramebuffer()

        // Render post process effects
        for (shader in shaders) {
            // Render shader to next framebuffer
            nextFramebuffer()
            shader.drawTo(currentFramebuffer())

            // Bind the rendered framebuffer
            boundFramebuffer = currentFramebuffer()
        }

        return boundFramebuffer
    }

    @JvmStatic
    fun renderShadersToFramebuffer(shaders: Collection<BlitProgram>, framebuffer: RenderTarget) {
        val renderedFramebuffer = renderShaders(shaders)
        copyFramebuffer(renderedFramebuffer, framebuffer)
    }

    /**
     * Copies [from] into [to] with a fullscreen blit.
     *
     * @param blend `null` copies with blending disabled (the previous `disableBlend = true`
     *              default); pass [BlendFunction.ADDITIVE] for the former GL_ONE/GL_ONE
     *              additive composition call sites (NOT LIGHTNING, which is SRC_ALPHA/ONE).
     */
    @JvmStatic
    @JvmOverloads
    fun copyFramebuffer(from: RenderTarget, to: RenderTarget, blend: BlendFunction? = null, copyDepth: Boolean = false) {
        val previousBound = boundFramebuffer
        boundFramebuffer = from
        val shader = if (copyDepth) blitShader else blitNoDepthShader
        shader.drawTo(to, blend)
        boundFramebuffer = previousBound
    }

    fun useFramebuffer(framebuffer: RenderTarget, action: () -> Unit) {
        val previousFramebuffer = sourceFramebuffer
        sourceFramebuffer = framebuffer

        clear(currentFramebuffer())
        copyFramebuffer(sourceFramebuffer, currentFramebuffer())
        boundFramebuffer = currentFramebuffer()

        action()
        sourceFramebuffer = previousFramebuffer
    }
}
