/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state.capabilities

import heckerpowered.matrix.client.render.state.RenderPipelineSnapshot
import heckerpowered.matrix.client.render.state.RenderPipelineState
import org.lwjgl.opengl.GL46.*

open class CapabilityState(val capability: Int, val enabled: Boolean) : RenderPipelineState {
    companion object {
        fun captureSnapshot(capability: Int): CapabilityState {
            val previousBlendState = glIsEnabled(capability)
            val snapshot = CapabilityState(capability, previousBlendState)
            return snapshot
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot(capability)

        if (enabled) {
            glEnable(capability)
        } else {
            glDisable(capability)
        }

        return RenderPipelineSnapshot(snapshot)
    }
}