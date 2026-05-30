/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.shader.*
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object CollapseEffectRenderer {
    var depthAttachment: Int = -1
    val dissolveFactor = SimpleDoubleAnimation(initValue = 0.0)

    val shader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/collapse.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(
            UniformProvider("depthAttachment") { pointer ->
                GL13.glActiveTexture(GL13.GL_TEXTURE0)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthAttachment)
                GL20.glUniform1i(pointer, 0)
            },
            inverseProjectionMatrixProvider,
            inverseViewMatrixProvider,
            resolutionProvider,
            playerPositionProvider,
            UniformProvider("dissolveFactor") { pointer ->
                GL20.glUniform1f(pointer, dissolveFactor.animatedValue.toFloat())
            },
            UniformProvider("noiseTexture") { pointer ->
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + 1)
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, DissolveShader.perlinNoiseTextureId)
                GL20.glUniform1i(pointer, 1)
            }
        )
    )
}