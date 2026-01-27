/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.ANGERED_EFFECT
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.Items
import net.minecraft.potion.Potion
import net.minecraft.potion.Potions
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry

val angeredPotion
    get() = MatrixPotions.angeredPotion
val angeredPotionEntry: RegistryEntry<Potion> by lazy { Registries.POTION.getEntry(angeredPotion) }

object MatrixPotions {

    val angeredPotion = register("angered", Potion(StatusEffectInstance(ANGERED_EFFECT, 20 * 30)))

    private fun register(name: String, potion: Potion): Potion {
        val identifier = Matrix.identifier(name)
        return Registry.register(Registries.POTION, identifier, potion)
    }

    fun onInitialize() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register { builder ->
            builder.registerPotionRecipe(
                Potions.AWKWARD,
                Items.SCULK_CATALYST,
                angeredPotionEntry
            )
        }
    }
}