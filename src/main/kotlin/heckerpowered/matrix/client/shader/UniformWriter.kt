/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import org.joml.Matrix4f

interface UniformWriter {
    fun findLocation(programObjectId: Int, uniformName: String): UniformLocation?

    fun writeFloat(location: UniformLocation, value: Float)
    fun writeInt(location: UniformLocation, value: Int)
    fun writeVector2f(location: UniformLocation, x: Float, y: Float)
    fun writeVector3f(location: UniformLocation, x: Float, y: Float, z: Float)
    fun writeVector4f(location: UniformLocation, x: Float, y: Float, z: Float, w: Float)
    fun writeMatrix4f(location: UniformLocation, matrix: Matrix4f, transpose: Boolean = false)
}