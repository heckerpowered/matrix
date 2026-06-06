/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.render.PostProcessRenderer
import net.minecraft.client.gl.Framebuffer

object BloomEffect {
    var brightnessThreshold = 1.0F
    var brightnessPassFramebuffer: Framebuffer = PostProcessRenderer.sourceFramebuffer
    val bloomDownFramebuffer: Framebuffer = PostProcessRenderer.createManagedFramebuffer()
    val bloomUpFramebuffer: Framebuffer = PostProcessRenderer.createManagedFramebuffer()
    fun renderBloom() = Unit
}
