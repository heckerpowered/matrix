/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import kotlin.math.abs

class FloatUniform(
    uniformName: String,
    programObjectId: Int,
    uniformWriter: UniformWriter,
    private val tolerance: Float = 0.0f,
) : Uniform<Float>(uniformName, programObjectId, uniformWriter) {
    override fun valuesEqual(a: Float, b: Float): Boolean {
        return abs(a - b) <= tolerance
    }

    override fun write(target: UniformLocation, value: Float) {
        uniformWriter.writeFloat(target, value)
    }
}