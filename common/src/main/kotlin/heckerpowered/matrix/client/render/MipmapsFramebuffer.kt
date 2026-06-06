/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import net.minecraft.client.gl.SimpleFramebuffer

class MipmapsFramebuffer(width: Int, height: Int, useDepth: Boolean = false, getError: Boolean = true) : SimpleFramebuffer(width, height, useDepth, getError) {
    var levelOfDetail: Int = 0
        set(value) {
            field = value
            viewportWidth = textureWidth shr value
            viewportHeight = textureHeight shr value
        }
}
