/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.tag

import heckerpowered.matrix.Matrix
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.level.Level

object MatrixDamageTypes {
    val magic = create("magic")

    private fun create(@Suppress("SameParameterValue") name: String): ResourceKey<DamageType> {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Matrix.identifier(name))
    }

    fun of(level: Level, key: ResourceKey<DamageType>): DamageSource {
        return DamageSource(
            level.registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .get(key.identifier()).orElseThrow()
        )
    }
}