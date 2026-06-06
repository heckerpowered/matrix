/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import net.minecraft.client.gl.Framebuffer

object OpacityMaskRenderer {
    fun render(opacityMaskFramebuffer: Framebuffer, colorFramebuffer: Framebuffer) = Unit
}

infix fun Framebuffer.opacityMask(colorFramebuffer: Framebuffer) {
    OpacityMaskRenderer.render(this, colorFramebuffer)
}
