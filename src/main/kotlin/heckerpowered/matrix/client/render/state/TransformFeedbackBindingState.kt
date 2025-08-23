/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.state

import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.GL40.*

class TransformFeedbackBindingState(val transformFeedbackId: Int) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): TransformFeedbackBindingState {
            val transformFeedbackId = glGetInteger(GL_TRANSFORM_FEEDBACK_BINDING)
            return TransformFeedbackBindingState(transformFeedbackId)
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, transformFeedbackId)

        return RenderPipelineSnapshot(snapshot)
    }
}