/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.state

import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.GL30.*

class FramebufferState(val framebuffer: Int) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): FramebufferState {
            val previousBindingFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING)
            val snapshot = FramebufferState(previousBindingFramebuffer)
            return snapshot
        }
    }

    constructor(framebuffer: Framebuffer) : this(framebuffer.fbo)

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer)

        return RenderPipelineSnapshot(snapshot)
    }
}