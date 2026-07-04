/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.ModMobEffects.Angered
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.Potions

object MatrixPotions {

    val angeredPotion = register("angered", MobEffectInstance(Angered, 20 * 30))

    private fun register(@Suppress("SameParameterValue") name: String, vararg effects: MobEffectInstance): Holder<Potion> {
        val identifier = Matrix.identifier(name)
        return Registry.registerForHolder(BuiltInRegistries.POTION, identifier, Potion(name, *effects))
    }

    fun onInitialize() {
        FabricPotionBrewingBuilder.BUILD.register { builder ->
            builder.addMix(
                Potions.AWKWARD,
                Items.SCULK_CATALYST,
                angeredPotion
            )
        }
    }
}