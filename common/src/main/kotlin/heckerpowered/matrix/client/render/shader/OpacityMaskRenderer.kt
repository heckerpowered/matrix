/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider

/**
 * A renderer that applies an opacity mask to discard fully transparent or fully black pixels.
 *
 * This class uses a custom fragment shader (`opacity_mask.fsh`) to perform per-pixel discarding based on the
 * following rules:
 *
 * - Discards a pixel if its **alpha channel is 0.0**.
 * - Discards a pixel if **all RGB channels are 0** (i.e., a fully black pixel).
 *
 * It uses two framebuffers:
 * - `opacityMaskFramebuffer`: Provides the mask texture to control pixel visibility.
 * - `colorFramebuffer`: Contains the source color data to be masked.
 *
 * During rendering, the appropriate color attachments from each framebuffer are bound to shader uniforms,
 * and a full-screen draw is performed with the mask applied into an explicit target.
 *
 * @see BlitProgram
 * @see RenderTarget
 */
object OpacityMaskRenderer {
    /**
     * The color attachment from the opacity mask framebuffer.
     */
    private var opacityMaskColorAttachmentView: GpuTextureView? = null

    /**
     * The color attachment from the color framebuffer.
     */
    private var colorAttachmentView: GpuTextureView? = null

    /**
     * The shader used to apply the opacity mask.
     *
     * It binds two texture inputs:
     * - `opacityMask` sampled from the mask framebuffer.
     * - `colorAttachment` sampled from the color framebuffer.
     *
     * The actual discard logic is implemented in the `opacity_mask.fsh` shader. No uniform
     * block is declared by this shader.
     */
    private val opacityMaskShader = BlitProgram(
        "post/opacity_mask.fsh",
        textures = arrayOf(
            TextureProvider("colorAttachment") { colorAttachmentView },
            TextureProvider("opacityMask") { opacityMaskColorAttachmentView }
        )
    )

    /**
     * Renders the opacity-masked result using the provided framebuffers.
     *
     * This method binds the color attachments from the given framebuffers to the shader's
     * texture inputs and draws the masked result into [target].
     *
     * @param opacityMaskFramebuffer The framebuffer containing the opacity mask texture.
     * @param colorFramebuffer The framebuffer containing the color texture to be masked.
     * @param target The render target to draw the masked result into. Defaults to
     *   [PostProcessRenderer.currentFramebuffer] since the old GL-era `render()` drew into
     *   whichever FBO happened to be bound, and there were no external callers to pin down a
     *   more specific default (verified via grep across common/src/main/kotlin).
     */
    fun render(
        opacityMaskFramebuffer: RenderTarget,
        colorFramebuffer: RenderTarget,
        target: RenderTarget = PostProcessRenderer.currentFramebuffer(),
        blend: BlendFunction? = null,
    ) {
        opacityMaskColorAttachmentView = opacityMaskFramebuffer.colorTextureView
        colorAttachmentView = colorFramebuffer.colorTextureView
        opacityMaskShader.drawTo(target, blend)
    }
}

/**
 * Applies an opacity mask from [mask] to [color], using [OpacityMaskRenderer], drawing the
 * masked result into [target].
 *
 * Replaces the former `infix fun RenderTarget.opacityMask(colorFramebuffer: RenderTarget)`:
 * an infix function cannot cleanly carry the third (explicit target) parameter the new
 * drawTo-based API requires, so this was changed to a regular 3-arg function. There were no
 * external callers of the old infix form (verified via grep across common/src/main/kotlin),
 * so this is a clean rename rather than an in-place signature change.
 *
 * @param mask The framebuffer providing the opacity mask (usually grayscale or alpha-based).
 * @param color The framebuffer whose contents are masked and rendered.
 * @param target The render target to draw the masked result into. Defaults to
 *   [PostProcessRenderer.currentFramebuffer].
 *
 * @see OpacityMaskRenderer
 */
fun applyOpacityMask(
    mask: RenderTarget,
    color: RenderTarget,
    target: RenderTarget = PostProcessRenderer.currentFramebuffer(),
) {
    OpacityMaskRenderer.render(mask, color, target)
}
