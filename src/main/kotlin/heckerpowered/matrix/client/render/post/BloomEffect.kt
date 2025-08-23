/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.event.InitAttachmentCallback
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.recommendMipLevel
import heckerpowered.matrix.client.render.shader.TentShader
import heckerpowered.matrix.client.render.state.BlendFuncSeparateState
import heckerpowered.matrix.client.render.state.FramebufferState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.render.state.ViewportState
import heckerpowered.matrix.client.render.state.capabilities.BlendState
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.FramebufferExtension.Companion.allocateMipmaps
import heckerpowered.matrix.core.FramebufferExtension.Companion.beginReadLod
import heckerpowered.matrix.core.FramebufferExtension.Companion.beginWriteLod
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL46.*
import org.slf4j.MarkerFactory

object BloomEffect {
    private val MARKER = MarkerFactory.getMarker("BLOOM_RENDERER")

    private val brightFramebuffer = PostProcessRenderer.createManagedFramebuffer()
    val bloomDownFramebuffer: Framebuffer = PostProcessRenderer.createManagedFramebuffer()
    val bloomUpFramebuffer: Framebuffer = PostProcessRenderer.createManagedFramebuffer()

    init {
        InitAttachmentCallback.EVENT.register(::onInitAttachment)

        bloomDownFramebuffer.resize(bloomDownFramebuffer.textureWidth, bloomDownFramebuffer.textureHeight, true)
        bloomUpFramebuffer.resize(bloomDownFramebuffer.textureWidth, bloomDownFramebuffer.textureHeight, true)
    }

    private fun onInitAttachment(framebuffer: Framebuffer) {
        if (framebuffer != bloomDownFramebuffer && framebuffer != bloomUpFramebuffer) {
            return
        }

        framebuffer.allocateMipmaps = true
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    }

    var brightnessPassFramebuffer: Framebuffer = minecraft.framebuffer
    var brightnessThreshold = 0F
    var bloomIntensity = 1.0F

    private val brightnessShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/bloom/bloom_brightness_pass.fsh", GL_FRAGMENT_SHADER),
            uniforms = arrayOf(
                UniformProvider("framebuffer") { pointer ->
                    val framebuffer = brightnessPassFramebuffer
                    glActiveTexture(GlConst.GL_TEXTURE0)
                    glBindTexture(GlConst.GL_TEXTURE_2D, framebuffer.colorAttachment)
                    glUniform1i(pointer, 0)
                },
                UniformProvider("threshold") { pointer ->
                    glUniform1f(pointer, brightnessThreshold)
                },
                UniformProvider("intensity") { pointer ->
                    glUniform1f(pointer, bloomIntensity)
                }
            )
        )
    }

    private fun clearBloomPasses(mipLevel: Int) {
        brightFramebuffer.clear(true)

        for (i in 0..<mipLevel) {
            bloomDownFramebuffer.beginWriteLod(i)
            bloomUpFramebuffer.beginWriteLod(i)

            bloomDownFramebuffer.clear(true)
            bloomUpFramebuffer.clear(true)
        }
    }

    private fun computeBloomPass() {
        PostProcessRenderer.renderShaderToFramebuffer(brightnessShader, brightFramebuffer)
    }

    private fun prepareDownsamplePass() {
        TentShader.framebufferObject = brightFramebuffer.colorAttachment
        bloomDownFramebuffer.beginWriteLod(0)
        PostProcessRenderer.copyFramebuffer(brightFramebuffer, bloomDownFramebuffer)
    }

    private fun generateDownsamplePasses(mipLevel: Int) {
        prepareDownsamplePass()

        TentShader.levelOfDetail = .0F
        for (i in 1..<mipLevel) {
            TentShader.framebufferObject = bloomDownFramebuffer.colorAttachment
            TentShader.levelOfDetail = i - 1.0F

            bloomDownFramebuffer.beginWriteLod(i)
            bloomDownFramebuffer.beginReadLod(i - 1)
            bloomDownFramebuffer.beginWrite(true)
            PostProcessRenderer.renderShaderToFramebuffer(TentShader.tentBlurShader, bloomDownFramebuffer)
        }
    }

    private fun prepareUpsamplePass(mipLevel: Int) {
        PostProcessRenderer.levelOfDetail = mipLevel - 1.0F
        TentShader.levelOfDetail = mipLevel - 1.0F
        bloomDownFramebuffer.beginReadLod(mipLevel - 1)
        bloomUpFramebuffer.beginWriteLod(mipLevel - 1)
        bloomUpFramebuffer.beginWrite(true)
        PostProcessRenderer.copyFramebuffer(bloomDownFramebuffer, bloomUpFramebuffer)

        RenderSystem.enableBlend()
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE)
    }

    private fun generateUpsamplePasses(mipLevel: Int) {
        prepareUpsamplePass(mipLevel)
        for (i in (0..<(mipLevel - 1)).reversed()) {
            // Copy LOD(n-1) to LOD(n)
            PostProcessRenderer.levelOfDetail = i + 1.0F
            bloomUpFramebuffer.beginReadLod(i + 1)
            bloomUpFramebuffer.beginWriteLod(i)
            bloomUpFramebuffer.beginWrite(true)
            PostProcessRenderer.copyFramebuffer(
                bloomUpFramebuffer /* LOD(n - 1) */,
                bloomUpFramebuffer /* LOD(n) */,
                false
            )

            TentShader.framebufferObject = bloomDownFramebuffer.colorAttachment
            bloomDownFramebuffer.beginReadLod(i)
            bloomUpFramebuffer.beginWriteLod(i)
            bloomUpFramebuffer.beginWrite(true)
            PostProcessRenderer.renderShaderToFramebuffer(TentShader.tentBlurShader, bloomUpFramebuffer, false)

            TentShader.levelOfDetail = i.toFloat()
        }
    }

    private fun resetBloomPasses() {
        bloomUpFramebuffer.beginReadLod(0)
        bloomUpFramebuffer.beginWriteLod(0)
    }

    private fun generateMipmaps(mipLevel: Int) {
        generateDownsamplePasses(mipLevel)
        generateUpsamplePasses(mipLevel)
    }

    fun renderBloom() {
        StateIsolation.isolate(
            FramebufferState.captureSnapshot(), ViewportState.captureSnapshot(),
            BlendState.captureSnapshot(), BlendFuncSeparateState.captureSnapshot()
        ) {
            val mipLevel = minecraft.framebuffer.recommendMipLevel()
            clearBloomPasses(mipLevel)
            computeBloomPass()
            generateMipmaps(mipLevel)
            resetBloomPasses()
        }
    }

    var renderBloom = false
}