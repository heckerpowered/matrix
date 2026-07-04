/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.DissolveShader
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider

// 26.2: post/dissolve/texture_dissolve.fsh declares
//   layout(std140) uniform MatrixPostUniforms { vec4 dissolveParams0; vec4 dissolveEmissiveColor; };
//   #define dissolveFactor dissolveParams0.x / emissiveRange .y / pixelStrength .z / detialStrength .w
// The old code never actually supplied emissiveRange/pixelStrength/detialStrength/emissiveColor as
// uniforms (they were presumably left at GLSL default-zero under the pre-std140 pipeline), so this
// port keeps the same effective values: dissolveFactor from state, the rest zeroed/white to preserve
// prior (likely degenerate/no-op) visuals bit-for-bit. See MatrixPostUniforms.kt@f25647a "post/dissolve/texture_dissolve"
// for the reference slot layout used here.
object TextureDissolveShader {
    var colorAttachment: GpuTextureView? = null
    var dissolveFactor: Float = 0F

    val program = BlitProgram(
        "post/dissolve/texture_dissolve.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                putVec4(dissolveFactor, 0.05F, 16.0F, 1.0F)
                putVec4(0F, 0.5F, 1.0F, 1.0F)
            }
        ),
        textures = arrayOf(
            TextureProvider("colorAttachment") { colorAttachment },
            TextureProvider("noiseTexture") { DissolveShader.perlinNoiseTextureView }
        )
    )
}