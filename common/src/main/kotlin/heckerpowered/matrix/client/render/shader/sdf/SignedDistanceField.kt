/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader.sdf

import heckerpowered.matrix.client.render.PostProcessRenderer
import net.minecraft.client.gl.Framebuffer

object SignedDistanceField {
    fun computeSignedDistanceField(source: Framebuffer, pingFramebuffer: Framebuffer, pongFramebuffer: Framebuffer): Framebuffer = pongFramebuffer
    fun computeSignedDistanceField(source: Framebuffer): Framebuffer = PostProcessRenderer.pong
}
