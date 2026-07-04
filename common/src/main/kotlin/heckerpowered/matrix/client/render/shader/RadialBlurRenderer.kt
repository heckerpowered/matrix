/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider

/**
 * A singleton object responsible for applying a radial blur post-processing effect.
 *
 * This renderer uses a custom shader to simulate radial blur by sampling pixels in a
 * radial pattern centered on the screen.
 *
 * ## Usage:
 * - Before calling `radialBlurShader.drawTo(target)`, set `colorAttachmentView`, `strength`, and `samples` as needed.
 *
 * ## Shader Inputs:
 * - `framebuffer`: The input color texture to be blurred.
 * - `strength`: Controls the intensity of the radial blur effect.
 * - `samples`: Determines how many samples are taken along the blur path.
 */
object RadialBlurRenderer {

    /** The color attachment to be blurred. */
    var colorAttachmentView: GpuTextureView? = null

    /** Controls how strong the radial blur effect appears. */
    var strength = 1.0F

    /** The number of samples taken to compute the blur effect. Higher = smoother blur. */
    var samples = 10

    /**
     * The shader responsible for executing the radial blur effect.
     *
     * Uses the standard fullscreen-triangle vertex stage and the `radial_blur.fsh` fragment
     * shader to perform the blur in a post-processing step. This is a separate [BlitProgram]
     * instance from the one `ScreenEffectRenderer` uses for the same shader path.
     */
    val radialBlurShader = BlitProgram(
        "post/blur/radial_blur.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                // MatrixPostData0 = vec4(strength, samples, 0, 0)
                putVec4(strength, samples.toFloat(), 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(
            TextureProvider("framebuffer") { colorAttachmentView }
        )
    )

    // TODO(26.2): `PostProcessEffect.blit()` (old enable-shader-then-blit-later pattern) had
    // no external callers (verified via grep across common/src/main/kotlin), so it was dropped
    // along with the `PostProcessEffect` interface implementation. Callers now draw explicitly
    // via `RadialBlurRenderer.radialBlurShader.drawTo(target)`.
}
