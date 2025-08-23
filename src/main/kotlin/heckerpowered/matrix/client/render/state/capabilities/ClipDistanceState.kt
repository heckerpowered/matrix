/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.state.capabilities

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.render.state.StateIsolation
import org.lwjgl.opengl.GL46.*

class ClipDistanceState(enabled: Boolean, index: Int = 0) : CapabilityState(GL_CLIP_DISTANCE0 + index, enabled) {
    companion object {
        val MAX_CLIP_DISTANCES by lazy { glGetInteger(GL_MAX_CLIP_DISTANCES) }
    }

    init {
        if (index >= MAX_CLIP_DISTANCES) {
            Matrix.LOGGER.error(StateIsolation.MARKER, "GL_CLIP_DISTANCE$index exceeds allowed maximum ${MAX_CLIP_DISTANCES - 1}. This will result in undefined behavior.")
        }
    }
}