/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.recipe

import com.mojang.serialization.MapCodec
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.item.isRedstoneSuit
import heckerpowered.matrix.common.item.redstoneSuitMaxPower
import heckerpowered.matrix.common.item.redstoneSuitPower
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.Level

object RedstoneSuitChargeRecipe : CustomRecipe() {

    private data class RedstoneCharger(val redstoneSuit: ItemStack, val redstone: ItemStack)

    private fun findCharger(input: CraftingInput): RedstoneCharger? {
        var redstoneSuit: ItemStack? = null
        var redstone: ItemStack? = null

        for (stack in input.items()) {
            when {
                redstoneSuit == null && stack.isRedstoneSuit() -> redstoneSuit = stack
                redstone == null && isRedstoneChargeItem(stack) -> redstone = stack
            }

            if (redstoneSuit != null && redstone != null) {
                return RedstoneCharger(redstoneSuit, redstone)
            }
        }

        return null
    }

    private fun isRedstoneChargeItem(stack: ItemStack): Boolean {
        return stack.item == Items.REDSTONE || stack.item == Items.REDSTONE_BLOCK
    }

    override fun matches(input: CraftingInput, level: Level): Boolean {
        val charger = findCharger(input) ?: return false
        return charger.redstoneSuit.redstoneSuitPower < charger.redstoneSuit.redstoneSuitMaxPower
    }

    override fun assemble(input: CraftingInput): ItemStack {
        val charger = findCharger(input) ?: return ItemStack.EMPTY
        val output = charger.redstoneSuit.copy()

        output.redstoneSuitPower += when (charger.redstone.item) {
            Items.REDSTONE -> 1
            Items.REDSTONE_BLOCK -> 9
            else -> 0
        }
        return output
    }

    val mapCodec: MapCodec<RedstoneSuitChargeRecipe> = MapCodec.unit(RedstoneSuitChargeRecipe)
    val streamCodec: StreamCodec<RegistryFriendlyByteBuf, RedstoneSuitChargeRecipe> = StreamCodec.unit<RegistryFriendlyByteBuf, RedstoneSuitChargeRecipe>(RedstoneSuitChargeRecipe)
    val recipeSerializer: RecipeSerializer<RedstoneSuitChargeRecipe> = RecipeSerializer<RedstoneSuitChargeRecipe>(mapCodec, streamCodec)

    override fun getSerializer(): RecipeSerializer<out CustomRecipe> {
        return recipeSerializer
    }

    fun onInitialize() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Matrix.identifier("redstone_suit_charger"), recipeSerializer)
    }
}
