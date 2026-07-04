/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider
import com.mojang.blaze3d.pipeline.RenderTarget

object ToneMapping {
    var exposureLinear: Float = 1.0f
    var exposureEv: Float = 0.0f

    private val toneMappingProgram by lazy {
        BlitProgram(
            "post/tone_mapping/aces_filmic.fsh",
            uniforms = arrayOf(
                UniformProvider("MatrixPostUniforms") {
                    // MatrixPostData0 = vec4(exposure, exposureEv, 0, 0)
                    putVec4(exposureLinear, exposureEv, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                }
            ),
            textures = arrayOf(
                TextureProvider("hdrScene") { currentSource.colorTextureView }
            )
        )
    }

    private lateinit var currentSource: RenderTarget
    val toneMapFramebuffer = PostProcessRenderer.createManagedFramebuffer()

    fun render(sourceFramebuffer: RenderTarget, targetFramebuffer: RenderTarget) {
        currentSource = sourceFramebuffer
        PostProcessRenderer.renderShaderToFramebuffer(toneMappingProgram, targetFramebuffer, null)
    }
}