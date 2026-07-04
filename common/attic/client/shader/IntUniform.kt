/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

class IntUniform(
    uniformName: String,
    programObjectId: Int,
    uniformWriter: UniformWriter,
) : Uniform<Int>(uniformName, programObjectId, uniformWriter) {
    override fun write(target: UniformLocation, value: Int) {
        uniformWriter.writeInt(target, value)
    }
}