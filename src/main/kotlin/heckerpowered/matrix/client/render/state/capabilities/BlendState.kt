/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.state.capabilities

import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.glIsEnabled

class BlendState(enabled: Boolean) : CapabilityState(GL_BLEND, enabled) {
    companion object {
        fun captureSnapshot(): BlendState {
            val previousBlendState = glIsEnabled(GL_BLEND)
            val snapshot = BlendState(previousBlendState)
            return snapshot
        }
    }
}

