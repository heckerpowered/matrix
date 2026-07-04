/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import org.joml.Vector4f

class Vector4fUniform(
    uniformName: String,
    programObjectId: Int,
    uniformWriter: UniformWriter,
) : Uniform<Vector4f>(uniformName, programObjectId, uniformWriter) {
    override fun write(target: UniformLocation, value: Vector4f) {
        uniformWriter.writeVector4f(target, value.x, value.y, value.z, value.w)
    }
}