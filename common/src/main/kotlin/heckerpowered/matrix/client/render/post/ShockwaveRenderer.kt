/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.*
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.core.times
import org.joml.Vector3f
import org.joml.Vector4f

object ShockwaveRenderer {
    var depthAttachment: GpuTextureView? = null

    var wavePosition: Vector3f = Vector3f()
    var waveColor = Vector4f(0.1F, 0.5F, 1.0F, 1.0F) * 4.0F
    var waveRadius = SimpleDoubleAnimation()
    var waveSize = SimpleDoubleAnimation()

    val shockwaveShader = BlitProgram(
        "post/shockwave.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                putInverseProjectionMatrix()
                putInverseViewMatrix()
                // MatrixPostData0.xyz = wavePosition, MatrixPostData0.w = waveRadius
                putVec4(wavePosition.x, wavePosition.y, wavePosition.z, waveRadius.animatedValue.toFloat())
                // MatrixPostData1 = waveColor
                putVec4(waveColor.x, waveColor.y, waveColor.z, waveColor.w)
                // MatrixPostData2.x = waveSize
                putVec4(waveSize.animatedValue.toFloat(), 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(
            // Pre-existing bug fixed: the .fsh declares `uniform sampler2D framebuffer;` (the
            // scene color it additively blends the wave color into) but the old Kotlin uniform
            // array never wired it up. Every other post shader in this codebase sources its
            // "previous pass output" sampler from PostProcessRenderer.framebufferProvider
            // (see RenderExtensions.kt / PostProcessRenderer.kt); used here for the same role.
            heckerpowered.matrix.client.render.framebufferProvider,
            TextureProvider("depthAttachment") { depthAttachment }
        )
    )
}
