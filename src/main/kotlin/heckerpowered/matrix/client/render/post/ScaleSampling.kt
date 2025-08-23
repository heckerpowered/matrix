/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.GlStateManager.Viewport
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER
import org.lwjgl.opengl.GL31

object ScaleSampling {
    private val downScalingFramebuffers = mutableMapOf<Double, ScalingFramebuffer>()
    private val upScalingFramebuffers = mutableMapOf<Double, ScalingFramebuffer>()

    fun getDownScalingFramebuffer(scaling: Double): ScalingFramebuffer {
        return downScalingFramebuffers.computeIfAbsent(scaling) {
            val framebuffer = ScalingFramebuffer(
                minecraft.window.framebufferWidth,
                minecraft.window.framebufferHeight,
                true,
                MinecraftClient.IS_SYSTEM_MAC,
                scaling
            )
            framebuffer.setClearColor(.0F, .0F, .0F, .0F)
            PostProcessRenderer.manageFramebuffer(framebuffer)
            framebuffer
        }
    }

    fun getUpScalingFramebuffer(scaling: Double): ScalingFramebuffer {
        return upScalingFramebuffers.computeIfAbsent(scaling) {
            val framebuffer = ScalingFramebuffer(
                minecraft.window.framebufferWidth,
                minecraft.window.framebufferHeight,
                true,
                MinecraftClient.IS_SYSTEM_MAC,
                scaling
            )
            framebuffer.setClearColor(.0F, .0F, .0F, .0F)
            PostProcessRenderer.manageFramebuffer(framebuffer)
            framebuffer
        }
    }

    fun createManagedScalingFramebuffer(scaling: Double): ScalingFramebuffer {
        val framebuffer = ScalingFramebuffer(
            minecraft.window.framebufferWidth,
            minecraft.window.framebufferHeight,
            true,
            MinecraftClient.IS_SYSTEM_MAC,
            scaling
        )
        framebuffer.setClearColor(.0F, .0F, .0F, .0F)
        PostProcessRenderer.manageFramebuffer(framebuffer)
        return framebuffer
    }

    fun clearAll() {
        downScalingFramebuffers.values.forEach { it.clear(false) }
        upScalingFramebuffers.values.forEach { it.clear(false) }
    }

    val framebuffer = PostProcessRenderer.createManagedFramebuffer()

    var levelOfDetail = 0F

    private var sourceFramebuffer: Framebuffer? = null
    private var targetFramebuffer: Framebuffer? = null

    private val sourceFramebufferProvider = UniformProvider("framebuffer") { pointer ->
        val sourceFramebuffer = sourceFramebuffer ?: return@UniformProvider

        GL31.glActiveTexture(GlConst.GL_TEXTURE0)
        GL31.glBindTexture(GlConst.GL_TEXTURE_2D, sourceFramebuffer.colorAttachment)

        RenderSystem.glUniform1i(pointer, 0)
    }

    private val sourceResolutionProvider = UniformProvider("sourceResolution") { pointer ->
        val framebuffer = sourceFramebuffer ?: return@UniformProvider

        GL31.glUniform2f(pointer, framebuffer.textureWidth.toFloat(), framebuffer.textureHeight.toFloat())
    }

    private val targetResolutionProvider = UniformProvider("targetResolution") { pointer ->
        val framebuffer = targetFramebuffer ?: return@UniformProvider

        GL31.glUniform2f(pointer, framebuffer.textureWidth.toFloat(), framebuffer.textureHeight.toFloat())
    }

    // Bi-linear sampling method
    val bilinearSample by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/sampling/bilinear.fsh", GL_FRAGMENT_SHADER),
            uniforms = arrayOf(sourceFramebufferProvider, sourceResolutionProvider, targetResolutionProvider)
        )
    }

    val textureLod by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/lower_sampling/lod.fsh", GL_FRAGMENT_SHADER),
            uniforms = arrayOf(
                sourceFramebufferProvider,
                UniformProvider("levelOfDetail") { pointer ->
                    GL31.glUniform1f(pointer, levelOfDetail)
                }
            )
        )
    }

    fun sample(sourceFramebuffer: Framebuffer, targetFramebuffer: Framebuffer, sampler: BlitProgram) {
        val previousFramebuffer = GlStateManager.getBoundFramebuffer()
        val previousViewportX = Viewport.getX()
        val previousViewportY = Viewport.getY()
        val previousViewportWidth = Viewport.getWidth()
        val previousViewportHeight = Viewport.getHeight()

        this.sourceFramebuffer = sourceFramebuffer
        this.targetFramebuffer = targetFramebuffer

        targetFramebuffer.beginWrite(true)
        sampler.blit()
        targetFramebuffer.endWrite()

        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, previousFramebuffer)
        GlStateManager._viewport(previousViewportX, previousViewportY, previousViewportWidth, previousViewportHeight)
    }
}