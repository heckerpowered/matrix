/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import net.minecraft.client.gl.SimpleFramebuffer
import org.lwjgl.opengl.GL46.*

class MipmapsFramebuffer(width: Int, height: Int, useDepth: Boolean = false, getError: Boolean = true) : SimpleFramebuffer(width, height, useDepth, getError) {
    init {
        glGenerateMipmap(fbo)
    }

    var levelOfDetail: Int = 0
        set(value) {
            field = value
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorAttachment, value)

            viewportWidth = textureWidth shr value
            viewportHeight = textureHeight shr value
            glViewport(0, 0, viewportWidth, viewportHeight)
        }
}