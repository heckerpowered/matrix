/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.tag

import heckerpowered.matrix.Matrix
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType

object MatrixDamageTypeTags {
    val magic = create("magic")

    private fun create(@Suppress("SameParameterValue") name: String): TagKey<DamageType> {
        return TagKey.create(Registries.DAMAGE_TYPE, Matrix.identifier(name))
    }
}