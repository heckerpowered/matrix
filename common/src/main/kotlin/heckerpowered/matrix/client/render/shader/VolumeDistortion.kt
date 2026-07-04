/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.client.shader.putInverseProjectionMatrix
import heckerpowered.matrix.client.shader.putInverseViewMatrix
import org.joml.Vector3f

object VolumeDistortion {
    var sceneColorTexture: GpuTextureView? = null
    var depthAttachment: GpuTextureView? = null

    var volumePosition: Vector3f = Vector3f()
    var volumeRadius: Float = 0F

    var grayscaleIntensity: Float = 0.0F
    var emissiveStrength: Float = 4.0F

    val Shader = BlitProgram(
        "post/volume_distortion.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                // mat4 inverseViewMatrix
                putInverseViewMatrix()
                // mat4 inverseProjectionMatrix
                putInverseProjectionMatrix()
                // volumeParams0 = volumePosition.xyz, volumeRadius
                putVec4(volumePosition.x, volumePosition.y, volumePosition.z, volumeRadius)
                // volumeParams1.x = grayscaleIntensity, .y = emissiveStrength
                putVec4(grayscaleIntensity, emissiveStrength, 0F, 0F)
            }
        ),
        textures = arrayOf(
            TextureProvider("sceneColorTexture") { sceneColorTexture },
            TextureProvider("depthAttachment") { depthAttachment }
        )
    )
}