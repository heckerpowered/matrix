/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state

class ShaderProgramState(val program: Any? = null) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): ShaderProgramState = ShaderProgramState()
    }

    override fun apply(): RenderPipelineSnapshot {
        return RenderPipelineSnapshot(captureSnapshot())
    }
}
