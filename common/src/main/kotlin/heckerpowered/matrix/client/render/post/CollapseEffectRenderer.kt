/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.client.shader.putInverseProjectionMatrix
import heckerpowered.matrix.client.shader.putInverseViewMatrix
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation

// 26.2: post/collapse.fsh declares
//   layout(std140) uniform MatrixPostUniforms {
//       mat4 inverseProjectionMatrix; mat4 inverseViewMatrix; vec4 MatrixPostData0; vec4 MatrixPostData1;
//   };
//   #define playerPosition MatrixPostData0.xyz
//   #define dissolveFactor MatrixPostData0.w
//   #define resolution MatrixPostData1.xy
// Note this shader has no `noiseTexture` sampler despite the old Kotlin wiring one up - the fragment
// shader synthesizes its own dissolve noise procedurally (valueNoise/hash13) and never samples a
// noise texture. The old `noiseTexture` UniformProvider was dead code; dropped here rather than
// carried forward as a TextureProvider with no matching sampler in the .fsh (BlitProgram only binds
// textures whose name matches a parsed sampler, so this would have been a silent no-op anyway).
object CollapseEffectRenderer {
    var depthAttachment: GpuTextureView? = null
    val dissolveFactor = SimpleDoubleAnimation(initValue = 0.0)

    val shader = BlitProgram(
        "post/collapse.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                putInverseProjectionMatrix()
                putInverseViewMatrix()
                val playerPosition = minecraft.player?.position()
                putVec4(
                    playerPosition?.x?.toFloat() ?: 0F,
                    playerPosition?.y?.toFloat() ?: 0F,
                    playerPosition?.z?.toFloat() ?: 0F,
                    dissolveFactor.animatedValue.toFloat()
                )
                putVec4(minecraft.window.width.toFloat(), minecraft.window.height.toFloat(), 0F, 0F)
            }
        ),
        textures = arrayOf(
            TextureProvider("depthAttachment") { depthAttachment }
        )
    )
}