/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.block.Block
import net.minecraft.item.Items
import net.minecraft.item.ToolMaterial
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey

val redstoneToolMaterial
    get() = MatrixToolMaterials.REDSTONE

val lapisLazuliToolMaterial
    get() = MatrixToolMaterials.LAPIS_LAZULI

val emeraldToolMaterial
    get() = MatrixToolMaterials.EMERALD

val coalToolMaterial
    get() = MatrixToolMaterials.COAL

enum class MatrixToolMaterials(
    private val inverseTag: TagKey<Block>,
    private val itemDurability: Int,
    private val miningSpeed: Float,
    private val attackDamage: Float,
    private val enchantability: Int,
    private val repairIngredient: () -> Ingredient,
) : ToolMaterial {
    REDSTONE(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 906, 7.0F, 2.0F, 12, {
        Ingredient.ofItems(Items.REDSTONE_BLOCK)
    }),
    LAPIS_LAZULI(BlockTags.INCORRECT_FOR_IRON_TOOL, 578, 6.0F, 2.0F, 50, {
        Ingredient.ofItems(Items.LAPIS_BLOCK)
    }),
    EMERALD(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1796, 8.5F, 3.5F, 12, {
        Ingredient.ofItems(Items.EMERALD)
    }),
    COAL(BlockTags.INCORRECT_FOR_IRON_TOOL, 191, 5F, 1.5F, 10, {
        Ingredient.ofItems(Items.COAL_BLOCK)
    });

    override fun getDurability() = itemDurability
    override fun getMiningSpeedMultiplier() = miningSpeed
    override fun getAttackDamage() = attackDamage
    override fun getInverseTag() = inverseTag
    override fun getEnchantability() = enchantability
    override fun getRepairIngredient() = repairIngredient()
}