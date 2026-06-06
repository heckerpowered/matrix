/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader.sdf

import net.minecraft.client.gl.Framebuffer

object DropShadowRenderer {
    var shadowSize = 0.0F
    fun render(signedDistanceField: Framebuffer) = Unit
}
