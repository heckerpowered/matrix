/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.core.extensions

import heckerpowered.matrix.core.MatrixMath.eulerToQuaternion
import net.minecraft.entity.Entity
import org.joml.Quaternionf

object EntityExtensions {
    val Entity.rotation: Quaternionf
        get() = eulerToQuaternion(yaw, pitch)
}