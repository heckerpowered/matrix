/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

/**
 * Represents a generic post-processing effect that can be applied to a framebuffer.
 *
 * Implementations of this interface define how a post-processing effect is applied,
 * such as blur, color correction, or distortion.
 */
interface PostProcessEffect {
    /**
     * The OpenGL texture ID of the color attachment to be processed.
     *
     * This is typically a texture rendered by the scene and used as input
     * for the post-processing effect.
     */
    var colorAttachment: Int

    /**
     * Applies the post-processing effect using the current shader and settings.
     *
     * This method should bind necessary resources, configure shaders,
     * and draw a fullscreen quad or appropriate geometry.
     */
    fun blit()
}