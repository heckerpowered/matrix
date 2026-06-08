/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader.sdf

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object SignedDistanceField {
    var stepSize = 1.0F

    private val seedGenShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/sdf/seed_gen.fsh", GL_FRAGMENT_SHADER),
    )

    private val jumpFloodingShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/sdf/jump_flooding.fsh", GL_FRAGMENT_SHADER),
    )

    private val sdfEvalShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/sdf/sdf_eval.fsh", GL_FRAGMENT_SHADER),
    )

    fun generateStepSizes(width: Int, height: Int): List<Int> {
        val maxResolution = maxOf(width, height).coerceAtLeast(1)
        var step = 1
        while (step * 2 <= maxResolution) {
            step *= 2
        }

        val steps = mutableListOf<Int>()
        while (step >= 1) {
            steps += step
            step /= 2
        }
        return steps
    }

    fun computeSignedDistanceField(source: Framebuffer, pingFramebuffer: Framebuffer, pongFramebuffer: Framebuffer): Framebuffer {
        PostProcessRenderer.renderShaderToFramebuffer(seedGenShader, source, pingFramebuffer)

        var input = pingFramebuffer
        var output = pongFramebuffer
        for (step in generateStepSizes(source.textureWidth, source.textureHeight)) {
            stepSize = step.toFloat()
            PostProcessRenderer.renderShaderToFramebuffer(jumpFloodingShader, input, output)

            val swap = input
            input = output
            output = swap
        }

        PostProcessRenderer.renderShaderToFramebuffer(
            sdfEvalShader,
            output,
            mapOf("framebuffer" to input, "originFramebuffer" to source),
        )
        return output
    }

    fun computeSignedDistanceField(source: Framebuffer): Framebuffer {
        PostProcessRenderer.resetFramebuffers()
        return computeSignedDistanceField(source, PostProcessRenderer.ping, PostProcessRenderer.pong)
    }
}
