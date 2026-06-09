/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.shader.DissolveShader
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import org.lwjgl.opengl.GL46.*

object TextureDissolveShader {
    var colorAttachment: Int = 0
    var dissolveFactor: Float = 0F

    val program = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/dissolve/texture_dissolve.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(
            UniformProvider("colorAttachment") { pointer ->
                glActiveTexture(GL_TEXTURE1)
                glBindTexture(GL_TEXTURE_2D, colorAttachment)
                glUniform1i(pointer, 1)
            },
            UniformProvider("noiseTexture") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, DissolveShader.perlinNoiseTextureId)
                glUniform1i(pointer, 0)
            },
            UniformProvider("dissolveFactor") { pointer ->
                glUniform1f(pointer, dissolveFactor)
            }
        )
    )
}
