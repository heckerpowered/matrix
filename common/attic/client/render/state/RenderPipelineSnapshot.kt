/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state

/**
 * Represents a snapshot of a previously applied render pipeline state.
 *
 * The snapshot holds a reference to the prior [RenderPipelineState] and allows restoring
 * that state by re-applying it to the current rendering pipeline.
 *
 * @author heckerpowered
 */
class RenderPipelineSnapshot(private val pipelineState: RenderPipelineState) {
    /**
     * Restores the render pipeline to the saved state by applying the stored [RenderPipelineState].
     */
    fun restore() {
        pipelineState.apply()
    }
}
