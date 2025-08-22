/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.state.capabilities

import org.lwjgl.opengl.GL11.glIsEnabled
import org.lwjgl.opengl.GL30.GL_RASTERIZER_DISCARD

class RasterizerDiscardState(enabled: Boolean) : CapabilityState(GL_RASTERIZER_DISCARD, enabled) {
    companion object {
        fun captureSnapshot(): RasterizerDiscardState {
            val previousState = glIsEnabled(GL_RASTERIZER_DISCARD)
            val snapshot = RasterizerDiscardState(previousState)
            return snapshot
        }
    }
}
