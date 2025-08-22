/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.core.math

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

object Vector3fExtensions {
    operator fun Vector3f.unaryMinus() = Vector3f(-x, -y, -z)

    operator fun Vector3f.times(matrix: Matrix4f): Vector4f {
        return matrix.transform(toVector4f())
    }

    fun Vector3f.toVector4f(): Vector4f {
        return Vector4f(x, y, z, 1.0F)
    }
}