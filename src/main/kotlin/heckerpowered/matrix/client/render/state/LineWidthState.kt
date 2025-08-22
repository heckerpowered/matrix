/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.state

import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL46.*

class LineWidthState(val lineWidth: Float, val shaderLineWidth: Float = lineWidth) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): LineWidthState {
            val lineWidth = glGetFloat(GL_LINE_WIDTH)
            val shaderLineWidth = RenderSystem.getShaderLineWidth()

            return LineWidthState(lineWidth, shaderLineWidth)
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        glLineWidth(lineWidth)
        RenderSystem.lineWidth(shaderLineWidth)

        return RenderPipelineSnapshot(snapshot)
    }
}