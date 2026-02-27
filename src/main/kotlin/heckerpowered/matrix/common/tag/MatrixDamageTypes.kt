/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.tag

import heckerpowered.matrix.Matrix
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageType
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.world.World

object MatrixDamageTypes {
    val magic = of("magic")

    private fun of(name: String): RegistryKey<DamageType> {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Matrix.identifier(name))
    }

    fun of(world: World, key: RegistryKey<DamageType>): DamageSource {
        return DamageSource(world.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(key))
    }
}