/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state

import net.minecraft.client.gl.Framebuffer

class ViewportState(val viewportX: Int, val viewportY: Int, val viewportWidth: Int, val viewportHeight: Int) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): ViewportState {
            return ViewportState(0, 0, 1, 1)
        }
    }

    constructor(framebuffer: Framebuffer) : this(0, 0, framebuffer.viewportWidth, framebuffer.viewportHeight)

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()
        return RenderPipelineSnapshot(snapshot)
    }
}
