/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import org.joml.Matrix4f
import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryStack

class MonolithicProgramUniformWriter(private val validateBinding: Boolean = true) : UniformWriter {
    private fun ensureProgramBound(programObjectId: Int) {
        if (!validateBinding) return
        val current = glGetInteger(GL_CURRENT_PROGRAM)
        if (current != programObjectId) {
            error("Program $programObjectId is not currently bound (current=$current). Bind the program before writing uniforms.")
        }
    }

    override fun findLocation(programObjectId: Int, uniformName: String): UniformLocation? {
        // glGetUniformLocation does not require the program to be bound.
        val locationIndex = glGetUniformLocation(programObjectId, uniformName)
        if (locationIndex < 0) return null
        return UniformLocation(programObjectId, locationIndex)
    }

    override fun writeFloat(location: UniformLocation, value: Float) {
        ensureProgramBound(location.programObjectId)
        glUniform1f(location.locationIndex, value)
    }

    override fun writeInt(location: UniformLocation, value: Int) {
        ensureProgramBound(location.programObjectId)
        glUniform1i(location.locationIndex, value)
    }

    override fun writeVector2f(location: UniformLocation, x: Float, y: Float) {
        ensureProgramBound(location.programObjectId)
        glUniform2f(location.locationIndex, x, y)
    }

    override fun writeVector3f(location: UniformLocation, x: Float, y: Float, z: Float) {
        ensureProgramBound(location.programObjectId)
        glUniform3f(location.locationIndex, x, y, z)
    }

    override fun writeVector4f(location: UniformLocation, x: Float, y: Float, z: Float, w: Float) {
        ensureProgramBound(location.programObjectId)
        glUniform4f(location.locationIndex, x, y, z, w)
    }

    override fun writeMatrix4f(location: UniformLocation, matrix: Matrix4f, transpose: Boolean) {
        ensureProgramBound(location.programObjectId)
        MemoryStack.stackPush().use { memoryStack ->
            val floatBuffer = memoryStack.mallocFloat(16)
            matrix.get(floatBuffer)
            glUniformMatrix4fv(location.locationIndex, transpose, floatBuffer)
        }
    }
}