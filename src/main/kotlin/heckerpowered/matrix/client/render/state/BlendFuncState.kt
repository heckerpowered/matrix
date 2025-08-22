/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.state

import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL46

class BlendFuncState(val srcFactor: Int, val dstFactor: Int) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): BlendFuncState {
            val previousSrcFactor = GL46.glGetInteger(GL46.GL_BLEND_SRC_RGB)
            val previousDstFactor = GL46.glGetInteger(GL46.GL_BLEND_DST_RGB)
            val snapshot = BlendFuncState(previousSrcFactor, previousDstFactor)
            return snapshot
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        glBlendFunc(srcFactor, dstFactor)

        return RenderPipelineSnapshot(snapshot)
    }
}