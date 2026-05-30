/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.shader.*
import org.lwjgl.opengl.GL46.*

object VortexRenderer {
    val vortexShader = Program(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/vortex/vortex.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(
            UniformProvider("noiseTexture") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, DissolveShader.perlinNoiseTextureId)
                glUniform1i(pointer, 0)
            },
            UniformProvider("time") { pointer ->
                glUniform1f(pointer, (System.currentTimeMillis().toDouble() / 1000.0 % 1000.0).toFloat())
            },
            UniformProvider("innerRadius") { pointer ->
                glUniform1f(pointer, 0.4F)
            },
            UniformProvider("outerRadius") { pointer ->
                glUniform1f(pointer, 0.5F)
            }
        )
    )

    val inverseVortexShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/vortex/inverse_vortex.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(
            UniformProvider("noiseTexture") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, DissolveShader.perlinNoiseTextureId)
                glUniform1i(pointer, 0)
            },
            UniformProvider("time") { pointer ->
                glUniform1f(pointer, (System.currentTimeMillis().toDouble() / 1000.0 % 1000.0).toFloat())
            },
            UniformProvider("innerRadius") { pointer ->
                glUniform1f(pointer, 1.0F)
            },
            UniformProvider("outerRadius") { pointer ->
                glUniform1f(pointer, 1.0F)
            },
            UniformProvider("feather") { pointer ->
                glUniform1f(pointer, 0.1F)
            }
        )
    )

    fun render() {

    }
}