/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
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
