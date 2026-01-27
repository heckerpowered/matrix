/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state

import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.GL15.*

class ArrayBufferState(val arrayBufferObject: Int) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): ArrayBufferState {
            val arrayBufferObject = glGetInteger(GL_ARRAY_BUFFER_BINDING)
            return ArrayBufferState(arrayBufferObject)
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        glBindBuffer(GL_ARRAY_BUFFER, arrayBufferObject)

        return RenderPipelineSnapshot(snapshot)
    }
}