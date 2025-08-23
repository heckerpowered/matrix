/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.state

import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.GL30.GL_VERTEX_ARRAY_BINDING
import org.lwjgl.opengl.GL30.glBindVertexArray

class VertexArrayState(val vertexArrayObject: Int) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): VertexArrayState {
            val vertexArrayObject = glGetInteger(GL_VERTEX_ARRAY_BINDING)
            return VertexArrayState(vertexArrayObject)
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        glBindVertexArray(vertexArrayObject)

        return RenderPipelineSnapshot(snapshot)
    }
}