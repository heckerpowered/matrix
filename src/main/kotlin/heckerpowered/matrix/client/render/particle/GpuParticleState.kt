/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.particle

import heckerpowered.matrix.client.render.particle.memory.MemoryLayout
import heckerpowered.matrix.client.render.particle.module.ParticleStateElement.*
import heckerpowered.matrix.client.render.state.ArrayBufferState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.render.state.VertexArrayState
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL46.*
import org.lwjgl.system.MemoryUtil

data class GpuParticleState(
    val vertexArrayObject: Int,
    var vertexBufferObjectPing: Int,
    var vertexBufferObjectPong: Int,
    val particleCount: Int,
    /**
     * The memory layout of input parameter of shader.
     */
    val layout: MemoryLayout,
) {
    companion object {
        fun defineAttributes(layout: MemoryLayout) {
            fun defineAttrib(index: Int, size: Int, offsetIndex: Int) {
                glEnableVertexAttribArray(index)
                glVertexAttribPointer(index, size, GL_FLOAT, false, layout.bufferSizeBytes, (offsetIndex * Float.SIZE_BYTES).toLong())
            }

            defineAttrib(0, 3, layout[POSITION_X])          // position.xyz
            defineAttrib(1, 3, layout[VELOCITY_X])          // velocity.xyz
            defineAttrib(2, 3, layout[ACCELERATION_X])      // acceleration.xyz
            defineAttrib(3, 1, layout[SPRITE_SIZE])         // sprite size
            defineAttrib(4, 1, layout[SCALE])               // size scale
            defineAttrib(5, 1, layout[AGE])                 // age
            defineAttrib(6, 1, layout[LIFETIME])            // lifetime
            defineAttrib(7, 4, layout[COLOR_R])             // color.rgba
            defineAttrib(8, 4, layout[ORIENTATION_X])       // orientation quaternion
            defineAttrib(9, 3, layout[ANGULAR_VELOCITY_X])  // angular velocity
        }

        fun createGpuParticleState(particleCount: Int, layout: MemoryLayout): GpuParticleState {
            val vertexArrayObject = glGenVertexArrays()
            val vertexBufferObjectPing = glGenBuffers()
            val vertexBufferObjectPong = glGenBuffers()

            glBindVertexArray(vertexArrayObject)

            val bufferSize = particleCount * layout.bufferSizeBytes.toLong()

            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectPing)
            glBufferData(GL_ARRAY_BUFFER, bufferSize, GL_DYNAMIC_DRAW)
            // defineAttributes(layout)

            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectPong)
            glBufferData(GL_ARRAY_BUFFER, bufferSize, GL_DYNAMIC_DRAW)
            // defineAttributes(layout)

            glBindVertexArray(0)
            glBindBuffer(GL_ARRAY_BUFFER, 0)

            return GpuParticleState(vertexArrayObject, vertexBufferObjectPing, vertexBufferObjectPong, particleCount, layout)
        }
    }

    fun bind() {
        glBindVertexArray(vertexArrayObject)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectPing)
        defineAttributes(layout)
    }

    fun bind(stateIsolation: StateIsolation) {
        stateIsolation.push(VertexArrayState(vertexArrayObject))
        stateIsolation.push(ArrayBufferState(vertexBufferObjectPing))
        defineAttributes(layout)
    }

    fun unbind() {
        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    fun swapBuffers() {
        val ping = vertexBufferObjectPing
        vertexBufferObjectPing = vertexBufferObjectPong
        vertexBufferObjectPong = ping
    }

    fun retrieve(): ParticleStateDump {
        return ParticleStateDump(this)
    }

    fun delete() {
        glDeleteVertexArrays(vertexArrayObject)
        glDeleteBuffers(vertexBufferObjectPing)
        glDeleteBuffers(vertexBufferObjectPong)
    }

    fun initParticles(initializer: (ParticleState) -> Unit) {
        val buffer = MemoryUtil.memAllocFloat(particleCount * layout.floats)
        buffer.clear()
        bind()
        repeat(particleCount) {
            val particleState = ParticleState(buffer.slice(it * layout.floats, layout.floats), layout)
            initializer(particleState)
        }
        buffer.rewind()
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_COPY)
        unbind()
        MemoryUtil.memFree(buffer)
    }
}