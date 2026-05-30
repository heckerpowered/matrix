/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.math

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

object Matrix4fExtensions {
    operator fun Matrix4f.times(other: Vector3f): Vector4f {
        return transform(other.toVector4f())
    }
}