/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state

class LineWidthState(val lineWidth: Float, val shaderLineWidth: Float = lineWidth) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): LineWidthState {
            return LineWidthState(1.0F)
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()
        return RenderPipelineSnapshot(snapshot)
    }
}
