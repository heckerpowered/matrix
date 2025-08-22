/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.core.extensions

import org.joml.Matrix4f
import org.joml.Quaternionf

object Matrix4fExtensions {
    operator fun Matrix4f.times(rotation: Quaternionf): Matrix4f {
        val result = Matrix4f()
        rotate(rotation, result)
        return result
    }

    operator fun Matrix4f.timesAssign(rotation: Quaternionf) {
        rotate(rotation)
    }
}