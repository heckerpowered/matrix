/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
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
    val noChain = of("no_chain")
}