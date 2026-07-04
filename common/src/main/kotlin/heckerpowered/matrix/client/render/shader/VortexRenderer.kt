/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.DissolveShader
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider

object VortexRenderer {
    val vortexShader = BlitProgram(
        "post/vortex/vortex.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                // vortexParams: x = time, y = innerRadius, z = outerRadius, w unused
                putVec4((System.currentTimeMillis().toDouble() / 1000.0 % 1000.0).toFloat(), 0.4F, 0.5F, 0F)
            }
        ),
        textures = arrayOf(
            TextureProvider("noiseTexture") { DissolveShader.perlinNoiseTextureView }
        )
    )

    val inverseVortexShader = BlitProgram(
        "post/vortex/inverse_vortex.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                // vortexParams: x = time, y = innerRadius, z = outerRadius, w = feather
                putVec4((System.currentTimeMillis().toDouble() / 1000.0 % 1000.0).toFloat(), 1.0F, 1.0F, 0.1F)
            }
        ),
        textures = arrayOf(
            TextureProvider("noiseTexture") { DissolveShader.perlinNoiseTextureView }
        )
    )

    fun render() {

    }
}