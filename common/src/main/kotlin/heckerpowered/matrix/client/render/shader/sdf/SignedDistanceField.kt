/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader.sdf

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.textureHeight
import heckerpowered.matrix.client.render.textureWidth
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider

object SignedDistanceField {
    var framebufferView: GpuTextureView? = null
    var originFramebufferView: GpuTextureView? = null
    var stepSize: Float = 1.0F

    init {
        RenderSystem.assertOnRenderThread()
    }

    val seedGenShader = BlitProgram(
        "post/sdf/seed_gen.fsh",
        textures = arrayOf(
            TextureProvider("framebuffer") { framebufferView }
        )
    )

    val jumpFloodingShader = BlitProgram(
        "post/sdf/jump_flooding.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                // MatrixPostData0.x = stepSize
                putVec4(stepSize, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(
            TextureProvider("framebuffer") { framebufferView }
        )
    )

    val sdfEvalShader = BlitProgram(
        "post/sdf/sdf_eval.fsh",
        textures = arrayOf(
            TextureProvider("framebuffer") { framebufferView },
            TextureProvider("originFramebuffer") { originFramebufferView }
        )
    )

    fun generateStepSizes(width: Int, height: Int): List<Int> {
        val maxRes = maxOf(width, height)
        var step = 1
        while (step * 2 <= maxRes) {
            step *= 2
        }
        val steps = mutableListOf<Int>()
        while (step >= 1) {
            steps.add(step)
            step /= 2
        }
        return steps
    }

    fun computeSignedDistanceField(source: RenderTarget, pingFramebuffer: RenderTarget, pongFramebuffer: RenderTarget): RenderTarget {
        framebufferView = source.colorTextureView
        originFramebufferView = source.colorTextureView
        PostProcessRenderer.renderShaderToFramebuffer(seedGenShader, pingFramebuffer)
        this.framebufferView = pingFramebuffer.colorTextureView

        val resolutionX = source.textureWidth
        val resolutionY = source.textureHeight

        var ping = pingFramebuffer
        var pong = pongFramebuffer
        for (stepSize in generateStepSizes(resolutionX, resolutionY)) {
            this.stepSize = stepSize.toFloat()
            val swap = ping
            ping = pong
            pong = swap

            framebufferView = pong.colorTextureView
            PostProcessRenderer.renderShaderToFramebuffer(jumpFloodingShader, ping)
        }

        framebufferView = ping.colorTextureView
        PostProcessRenderer.renderShaderToFramebuffer(sdfEvalShader, pong)
        return pong
    }

    fun computeSignedDistanceField(source: RenderTarget): RenderTarget {
        PostProcessRenderer.resetFramebuffers()
        return computeSignedDistanceField(source, PostProcessRenderer.ping, PostProcessRenderer.pong)
    }
}
