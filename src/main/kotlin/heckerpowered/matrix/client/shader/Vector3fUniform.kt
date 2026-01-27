/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import org.joml.Vector3f

class Vector3fUniform(
    uniformName: String,
    programObjectId: Int,
    uniformWriter: UniformWriter,
) : Uniform<Vector3f>(uniformName, programObjectId, uniformWriter) {
    override fun write(target: UniformLocation, value: Vector3f) {
        uniformWriter.writeVector3f(target, value.x, value.y, value.z)
    }
}