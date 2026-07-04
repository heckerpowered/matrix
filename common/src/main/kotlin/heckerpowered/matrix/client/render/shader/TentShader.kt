/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider

object TentShader {
    /**
     * The color attachment to sample from. Kept as `framebufferObject` for source
     * compatibility with existing call sites (e.g. BloomEffect.kt), retyped from the
     * old GL texture id (`Int`) to a [GpuTextureView].
     */
    var framebufferObject: GpuTextureView? = null
    var levelOfDetail = .0F

    val tentBlurShader = BlitProgram(
        "post/blur/tent.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                // MatrixPostData0.x = lod
                putVec4(levelOfDetail, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(
            TextureProvider("framebuffer") { framebufferObject }
        )
    )

    // TODO(26.2): enable(framebufferObject, levelOfDetail)/disable() removed — the old
    // enable-shader-then-blit-later pattern has no equivalent under the wrapper API.
    // Callers must now set `framebufferObject`/`levelOfDetail` directly and then call
    // `tentBlurShader.drawTo(target)` themselves.
}
