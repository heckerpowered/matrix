/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import org.joml.Vector3f
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object VolumeDistortion {
    var sceneColorTexture: Int = 0
    var depthAttachment: Int = 0

    var volumePosition: Vector3f = Vector3f()
    var volumeRadius: Float = 0F

    var grayscaleIntensity: Float = 0.0F
    var emissiveStrength: Float = 4.0F

    val Shader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/volume_distortion.fsh", GL_FRAGMENT_SHADER),
    )
}
