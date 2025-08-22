/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.tag

import heckerpowered.matrix.Matrix
import net.minecraft.entity.damage.DamageType
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

object MatrixDamageTypeTags {
    private fun of(id: String): TagKey<DamageType> {
        return TagKey.of(RegistryKeys.DAMAGE_TYPE, Matrix.identifier(id))
    }

    val magic = of("magic")
}