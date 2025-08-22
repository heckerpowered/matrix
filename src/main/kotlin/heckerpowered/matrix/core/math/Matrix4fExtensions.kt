/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.core.math

import heckerpowered.matrix.core.math.Vector3fExtensions.toVector4f
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

object Matrix4fExtensions {
    operator fun Matrix4f.times(other: Vector3f): Vector4f {
        return transform(other.toVector4f())
    }
}