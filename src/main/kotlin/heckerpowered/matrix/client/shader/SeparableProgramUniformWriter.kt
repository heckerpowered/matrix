/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import org.joml.Matrix4f
import org.lwjgl.opengl.GL41.*
import org.lwjgl.system.MemoryStack

class SeparableProgramUniformWriter : UniformWriter {
    override fun findLocation(programObjectId: Int, uniformName: String): UniformLocation? {
        val index = glGetUniformLocation(programObjectId, uniformName)
        if (index < 0) return null
        return UniformLocation(programObjectId, index)
    }

    override fun writeFloat(location: UniformLocation, value: Float) {
        glProgramUniform1f(location.programObjectId, location.locationIndex, value)
    }

    override fun writeInt(location: UniformLocation, value: Int) {
        glProgramUniform1i(location.programObjectId, location.locationIndex, value)
    }

    override fun writeVector2f(location: UniformLocation, x: Float, y: Float) {
        glProgramUniform2f(location.programObjectId, location.locationIndex, x, y)
    }

    override fun writeVector3f(location: UniformLocation, x: Float, y: Float, z: Float) {
        glProgramUniform3f(location.programObjectId, location.locationIndex, x, y, z)
    }

    override fun writeVector4f(location: UniformLocation, x: Float, y: Float, z: Float, w: Float) {
        glProgramUniform4f(location.programObjectId, location.locationIndex, x, y, z, w)
    }

    override fun writeMatrix4f(location: UniformLocation, matrix: Matrix4f, transpose: Boolean) {
        MemoryStack.stackPush().use { memoryStack ->
            val floatBuffer = memoryStack.mallocFloat(16)
            matrix.get(floatBuffer)
            glProgramUniformMatrix4fv(location.programObjectId, location.locationIndex, transpose, floatBuffer)
        }
    }
}