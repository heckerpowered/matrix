/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state

import org.lwjgl.opengl.GL46.*

class BlendFuncSeparateState(
    val srcFactorRGB: Int = GL_SRC_ALPHA,
    val dstFactorRGB: Int = GL_ONE_MINUS_SRC_ALPHA,
    val srcFactorAlpha: Int = GL_ONE,
    val dstFactorAlpha: Int = GL_ZERO,
) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): BlendFuncSeparateState {
            val previousSrcFactorRGB = glGetInteger(GL_BLEND_SRC_RGB)
            val previousDstFactorRGB = glGetInteger(GL_BLEND_DST_RGB)
            val previousSrcFactorAlpha = glGetInteger(GL_BLEND_SRC_ALPHA)
            val previousDstFactorAlpha = glGetInteger(GL_BLEND_DST_ALPHA)
            val snapshot = BlendFuncSeparateState(previousSrcFactorRGB, previousDstFactorRGB, previousSrcFactorAlpha, previousDstFactorAlpha)
            return snapshot
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        glBlendFuncSeparate(srcFactorRGB, dstFactorRGB, srcFactorAlpha, dstFactorAlpha)

        return RenderPipelineSnapshot(snapshot)
    }
}