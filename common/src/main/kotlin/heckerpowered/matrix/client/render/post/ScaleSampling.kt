/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider
import com.mojang.blaze3d.pipeline.RenderTarget

object ScaleSampling {
    private val downScalingFramebuffers = mutableMapOf<Double, ScalingFramebuffer>()
    private val upScalingFramebuffers = mutableMapOf<Double, ScalingFramebuffer>()

    fun getDownScalingFramebuffer(scaling: Double): ScalingFramebuffer {
        return downScalingFramebuffers.computeIfAbsent(scaling) {
            val framebuffer = ScalingFramebuffer(
                "matrix downscale $scaling",
                minecraft.window.width,
                minecraft.window.height,
                true,
                scaling
            )
            PostProcessRenderer.manageFramebuffer(framebuffer)
            framebuffer
        }
    }

    fun getUpScalingFramebuffer(scaling: Double): ScalingFramebuffer {
        return upScalingFramebuffers.computeIfAbsent(scaling) {
            val framebuffer = ScalingFramebuffer(
                "matrix upscale $scaling",
                minecraft.window.width,
                minecraft.window.height,
                true,
                scaling
            )
            PostProcessRenderer.manageFramebuffer(framebuffer)
            framebuffer
        }
    }

    fun createManagedScalingFramebuffer(scaling: Double): ScalingFramebuffer {
        val framebuffer = ScalingFramebuffer(
            "matrix managed scale $scaling",
            minecraft.window.width,
            minecraft.window.height,
            true,
            scaling
        )
        PostProcessRenderer.manageFramebuffer(framebuffer)
        return framebuffer
    }

    fun clearAll() {
        downScalingFramebuffers.values.forEach { PostProcessRenderer.clear(it) }
        upScalingFramebuffers.values.forEach { PostProcessRenderer.clear(it) }
    }

    val framebuffer = PostProcessRenderer.createManagedFramebuffer()

    var levelOfDetail = 0F

    private var sourceFramebuffer: RenderTarget? = null
    private var targetFramebuffer: RenderTarget? = null

    private val sourceFramebufferProvider = TextureProvider("framebuffer") {
        sourceFramebuffer?.colorTextureView
    }

    // Bi-linear sampling method
    val bilinearSample by lazy {
        BlitProgram(
            "post/sampling/bilinear.fsh",
            uniforms = arrayOf(
                UniformProvider("MatrixPostUniforms") {
                    // MatrixPostData0 = vec4(sourceResolution, targetResolution)
                    putVec4(
                        sourceFramebuffer?.width?.toFloat() ?: 0F,
                        sourceFramebuffer?.height?.toFloat() ?: 0F,
                        targetFramebuffer?.width?.toFloat() ?: 0F,
                        targetFramebuffer?.height?.toFloat() ?: 0F
                    )
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                }
            ),
            textures = arrayOf(sourceFramebufferProvider)
        )
    }

    val textureLod by lazy {
        BlitProgram(
            "post/lower_sampling/lod.fsh",
            uniforms = arrayOf(
                UniformProvider("MatrixPostUniforms") {
                    // MatrixPostData0.x = levelOfDetail
                    putVec4(levelOfDetail, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                }
            ),
            textures = arrayOf(sourceFramebufferProvider)
        )
    }

    fun sample(sourceFramebuffer: RenderTarget, targetFramebuffer: RenderTarget, sampler: BlitProgram) {
        this.sourceFramebuffer = sourceFramebuffer
        this.targetFramebuffer = targetFramebuffer

        sampler.drawTo(targetFramebuffer)
    }
}