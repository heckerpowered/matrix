/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import org.joml.Vector2f

class Vector2fUniform(
    uniformName: String,
    programObjectId: Int,
    uniformWriter: UniformWriter,
) : Uniform<Vector2f>(uniformName, programObjectId, uniformWriter) {
    override fun write(target: UniformLocation, value: Vector2f) {
        uniformWriter.writeVector2f(target, value.x, value.y)
    }
}