/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object ScaleSampling {
    private val downScalingFramebuffers = mutableMapOf<Double, ScalingFramebuffer>()
    private val upScalingFramebuffers = mutableMapOf<Double, ScalingFramebuffer>()

    var levelOfDetail = .0F

    val bilinearSample by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/sampling/bilinear.fsh", GL_FRAGMENT_SHADER),
        )
    }

    val textureLod by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/lower_sampling/lod.fsh", GL_FRAGMENT_SHADER),
        )
    }

    fun createManagedScalingFramebuffer(scaling: Double): Framebuffer {
        return ScalingFramebuffer(scaling, framebufferWidth(), framebufferHeight(), true, false).also { framebuffer ->
            framebuffer.setClearColor(.0F, .0F, .0F, .0F)
            PostProcessRenderer.manageFramebuffer(framebuffer)
        }
    }

    fun getDownScalingFramebuffer(scaling: Double): Framebuffer {
        return downScalingFramebuffers.getOrPut(scaling) {
            createManagedScalingFramebuffer(scaling) as ScalingFramebuffer
        }
    }

    fun getUpScalingFramebuffer(scaling: Double): Framebuffer {
        return upScalingFramebuffers.getOrPut(scaling) {
            createManagedScalingFramebuffer(scaling) as ScalingFramebuffer
        }
    }

    fun clearAll() {
        downScalingFramebuffers.values.forEach { it.clear(false) }
        upScalingFramebuffers.values.forEach { it.clear(false) }
    }

    fun sample(sourceFramebuffer: Framebuffer, targetFramebuffer: Framebuffer, sampler: BlitProgram) {
        PostProcessRenderer.renderShaderToFramebuffer(sampler, sourceFramebuffer, targetFramebuffer)
    }

    private fun framebufferWidth(): Int {
        return Minecraft.getInstance().gameRenderer.mainRenderTarget().width.coerceAtLeast(1)
    }

    private fun framebufferHeight(): Int {
        return Minecraft.getInstance().gameRenderer.mainRenderTarget().height.coerceAtLeast(1)
    }
}
