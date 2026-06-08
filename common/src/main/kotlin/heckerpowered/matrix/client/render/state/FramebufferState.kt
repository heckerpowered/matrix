/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state

import net.minecraft.client.gl.Framebuffer

class FramebufferState(val framebuffer: Int) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): FramebufferState {
            return FramebufferState(0)
        }
    }

    constructor(framebuffer: Framebuffer) : this(framebuffer.fbo)

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()
        return RenderPipelineSnapshot(snapshot)
    }
}
