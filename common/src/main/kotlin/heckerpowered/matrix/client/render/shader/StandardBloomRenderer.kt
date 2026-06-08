/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.render.post.BloomEffect
import net.minecraft.client.gl.Framebuffer

object StandardBloomRenderer {
    fun render(brightnessPass: Framebuffer) {
        BloomEffect.brightnessPassFramebuffer = brightnessPass
        BloomEffect.renderBloom()
    }
}
