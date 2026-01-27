/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import org.joml.Matrix4f

class Matrix4fUniform(
    uniformName: String,
    programObjectId: Int,
    uniformWriter: UniformWriter,
    private val transpose: Boolean = false,
) : Uniform<Matrix4f>(uniformName, programObjectId, uniformWriter) {
    override fun write(target: UniformLocation, value: Matrix4f) {
        uniformWriter.writeMatrix4f(target, value, transpose)
    }
}