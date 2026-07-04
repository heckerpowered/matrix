/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.textures.GpuTextureView

// 26.2: post/dissolve/texture_pixel_dissolve.fsh declares
//   layout(std140) uniform MatrixPostUniforms { vec4 dissolveParams0; vec4 dissolveEmissiveColor; };
//   #define dissolveFactor dissolveParams0.x / emissiveRange .y / pixelStrength .z / detialStrength .w
// The old code never actually supplied emissiveRange/pixelStrength/detialStrength/emissiveColor as
// uniforms (GLSL defaults applied under the pre-std140 pipeline: emissiveRange=0.05, pixelStrength=100.0,
// detialStrength=1.0, emissiveColor=vec4(0,0.5,1.0,1.0)), so this port keeps those exact values.
// See MatrixPostUniforms.kt@f25647a "post/dissolve/texture_pixel_dissolve" for the reference slot layout.
//
// The mutable state lives in a separate holder because Kotlin forbids the super-constructor
// lambdas from capturing the object's own (not yet initialized) instance.
private object TexturePixelDissolveState {
    var noiseTexture: GpuTextureView? = null
    var normalTexture: GpuTextureView? = null
    var dissolveFactor: Float = 1.0f
}

object TexturePixelDissolveProgram : BlitProgram(
    "post/dissolve/texture_pixel_dissolve.fsh",
    uniforms = arrayOf(
        UniformProvider("MatrixPostUniforms") {
            putVec4(TexturePixelDissolveState.dissolveFactor, 0.05F, 100.0F, 1.0F)
            putVec4(0F, 0.5F, 1.0F, 1.0F)
        }
    ),
    textures = arrayOf(
        TextureProvider("noiseTexture") {
            TexturePixelDissolveState.noiseTexture ?: DissolveShader.perlinNoiseTextureView
        },
        TextureProvider("normalTexture") { TexturePixelDissolveState.normalTexture }
    )
) {
    var noiseTexture: GpuTextureView?
        get() = TexturePixelDissolveState.noiseTexture
        set(value) {
            TexturePixelDissolveState.noiseTexture = value
        }

    var normalTexture: GpuTextureView?
        get() = TexturePixelDissolveState.normalTexture
        set(value) {
            TexturePixelDissolveState.normalTexture = value
        }

    var dissolveFactor: Float
        get() = TexturePixelDissolveState.dissolveFactor
        set(value) {
            TexturePixelDissolveState.dissolveFactor = value
        }
}
