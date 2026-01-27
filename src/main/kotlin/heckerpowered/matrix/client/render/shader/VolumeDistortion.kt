/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.shader.*
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL20.*

object VolumeDistortion {
    var sceneColorTexture: Int = 0
    var depthAttachment: Int = 0

    var volumePosition: Vector3f = Vector3f()
    var volumeRadius: Float = 0F

    var grayscaleIntensity: Float = 0.0F
    var emissiveStrength: Float = 4.0F

    val Shader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/volume_distortion.frag", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(
            UniformProvider("sceneColorTexture") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, sceneColorTexture)
                glUniform1i(pointer, 0)
            },
            UniformProvider("depthAttachment") { pointer ->
                glActiveTexture(GL_TEXTURE1)
                glBindTexture(GL_TEXTURE_2D, depthAttachment)
                glUniform1i(pointer, 1)
            },
            // projectionMatrixProvider,
            // viewProjectionMatrixProvider,
            inverseViewMatrixProvider,
            inverseProjectionMatrixProvider,
            UniformProvider("volumePosition") { pointer ->
                glUniform3f(pointer, volumePosition.x, volumePosition.y, volumePosition.z)
            },
            UniformProvider("volumeRadius") { pointer ->
                glUniform1f(pointer, volumeRadius)
            },
            UniformProvider("grayscaleIntensity") { pointer ->
                glUniform1f(pointer, grayscaleIntensity)
            },
            UniformProvider("emissiveStrength") { pointer ->
                glUniform1f(pointer, emissiveStrength)
            }
        )
    )
}