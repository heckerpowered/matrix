/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.render.PostProcessRenderer
import net.minecraft.client.gl.Framebuffer

object ToneMapping {
    var exposureLinear = 1.0F
    var exposureEv = 0.0F
    val toneMapFramebuffer: Framebuffer = PostProcessRenderer.createManagedFramebuffer()
    fun render(sourceFramebuffer: Framebuffer, targetFramebuffer: Framebuffer) = Unit
}
