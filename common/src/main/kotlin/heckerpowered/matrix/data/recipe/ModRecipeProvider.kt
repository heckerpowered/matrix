/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.data.recipe

import heckerpowered.matrix.common.item.CoalAxeItem
import heckerpowered.matrix.common.item.CoalBootsItem
import heckerpowered.matrix.common.item.CoalChestplateItem
import heckerpowered.matrix.common.item.CoalHelmetItem
import heckerpowered.matrix.common.item.CoalHoeItem
import heckerpowered.matrix.common.item.CoalLeggingsItem
import heckerpowered.matrix.common.item.CoalPickaxeItem
import heckerpowered.matrix.common.item.CoalShovelItem
import heckerpowered.matrix.common.item.CoalSwordItem
import heckerpowered.matrix.common.item.EmeraldAxeItem
import heckerpowered.matrix.common.item.EmeraldBootsItem
import heckerpowered.matrix.common.item.EmeraldChestplateItem
import heckerpowered.matrix.common.item.EmeraldHelmetItem
import heckerpowered.matrix.common.item.EmeraldHoeItem
import heckerpowered.matrix.common.item.EmeraldLeggingsItem
import heckerpowered.matrix.common.item.EmeraldPickaxeItem
import heckerpowered.matrix.common.item.EmeraldShovelItem
import heckerpowered.matrix.common.item.EmeraldSwordItem
import heckerpowered.matrix.common.item.LapisLazuliAxeItem
import heckerpowered.matrix.common.item.LapisLazuliBootsItem
import heckerpowered.matrix.common.item.LapisLazuliChestplateItem
import heckerpowered.matrix.common.item.LapisLazuliHelmetItem
import heckerpowered.matrix.common.item.LapisLazuliHoeItem
import heckerpowered.matrix.common.item.LapisLazuliLeggingsItem
import heckerpowered.matrix.common.item.LapisLazuliPickaxeItem
import heckerpowered.matrix.common.item.LapisLazuliShovelItem
import heckerpowered.matrix.common.item.LapisLazuliSwordItem
import heckerpowered.matrix.common.item.RedstoneAxeItem
import heckerpowered.matrix.common.item.RedstoneBootsItem
import heckerpowered.matrix.common.item.RedstoneChestplateItem
import heckerpowered.matrix.common.item.RedstoneHelmetItem
import heckerpowered.matrix.common.item.RedstoneHoeItem
import heckerpowered.matrix.common.item.RedstoneLeggingsItem
import heckerpowered.matrix.common.item.RedstonePickaxeItem
import heckerpowered.matrix.common.item.RedstoneShovelItem
import heckerpowered.matrix.common.item.RedstoneSwordItem
import heckerpowered.matrix.common.item.StoneBootsItem
import heckerpowered.matrix.common.item.StoneChestplateItem
import heckerpowered.matrix.common.item.StoneHelmetItem
import heckerpowered.matrix.common.item.StoneLeggingsItem
import heckerpowered.matrix.common.item.WoodenBootsItem
import heckerpowered.matrix.common.item.WoodenChestplateItem
import heckerpowered.matrix.common.item.WoodenHelmetItem
import heckerpowered.matrix.common.item.WoodenLeggingsItem
import heckerpowered.matrix.common.recipe.RedstoneSuitChargeRecipe
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.core.HolderLookup
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.data.recipes.SpecialRecipeBuilder.special
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import java.util.concurrent.CompletableFuture

class ModRecipeProvider(
    fabricPackOutput: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>,
) : FabricRecipeProvider(fabricPackOutput, registriesFuture) {
    override fun createRecipeProvider(registries: HolderLookup.Provider, output: RecipeOutput): RecipeProvider {
        return object : RecipeProvider(registries, output) {
            override fun buildRecipes() {
                buildRedstoneSuitRecipes(output)
                buildLapisLazuliSuitRecipes(output)
                buildEmeraldSuitRecipes(output)
                buildCoalSuitRecipes(output)
                buildStoneSuitRecipes(output)
                buildWoodenSuitRecipes(output)

                special { RedstoneSuitChargeRecipe }
                    .save(output, "matrix:redstone_suit_charge")
            }

            private fun buildRedstoneSuitRecipes(output: RecipeOutput) {
                buildArmorRecipes(
                    output = output,
                    group = "matrix_redstone_suit",
                    material = Items.REDSTONE_BLOCK,
                    helmet = RedstoneHelmetItem,
                    chestplate = RedstoneChestplateItem,
                    leggings = RedstoneLeggingsItem,
                    boots = RedstoneBootsItem,
                )

                buildToolRecipes(
                    output = output,
                    group = "matrix_redstone_suit",
                    material = Items.REDSTONE_BLOCK,
                    sword = RedstoneSwordItem,
                    pickaxe = RedstonePickaxeItem,
                    axe = RedstoneAxeItem,
                    shovel = RedstoneShovelItem,
                    hoe = RedstoneHoeItem,
                )
            }

            private fun buildLapisLazuliSuitRecipes(output: RecipeOutput) {
                buildArmorRecipes(
                    output = output,
                    group = "matrix_lapis_lazuli_suit",
                    material = Items.LAPIS_BLOCK,
                    helmet = LapisLazuliHelmetItem,
                    chestplate = LapisLazuliChestplateItem,
                    leggings = LapisLazuliLeggingsItem,
                    boots = LapisLazuliBootsItem,
                )

                buildToolRecipes(
                    output = output,
                    group = "matrix_lapis_lazuli_suit",
                    material = Items.LAPIS_BLOCK,
                    sword = LapisLazuliSwordItem,
                    pickaxe = LapisLazuliPickaxeItem,
                    axe = LapisLazuliAxeItem,
                    shovel = LapisLazuliShovelItem,
                    hoe = LapisLazuliHoeItem,
                )
            }

            private fun buildEmeraldSuitRecipes(output: RecipeOutput) {
                buildArmorRecipes(
                    output = output,
                    group = "matrix_emerald_suit",
                    material = Items.EMERALD,
                    helmet = EmeraldHelmetItem,
                    chestplate = EmeraldChestplateItem,
                    leggings = EmeraldLeggingsItem,
                    boots = EmeraldBootsItem,
                )

                buildToolRecipes(
                    output = output,
                    group = "matrix_emerald_suit",
                    material = Items.EMERALD,
                    sword = EmeraldSwordItem,
                    pickaxe = EmeraldPickaxeItem,
                    axe = EmeraldAxeItem,
                    shovel = EmeraldShovelItem,
                    hoe = EmeraldHoeItem,
                )
            }

            private fun buildCoalSuitRecipes(output: RecipeOutput) {
                buildArmorRecipes(
                    output = output,
                    group = "matrix_coal_suit",
                    material = Items.COAL_BLOCK,
                    helmet = CoalHelmetItem,
                    chestplate = CoalChestplateItem,
                    leggings = CoalLeggingsItem,
                    boots = CoalBootsItem,
                )

                buildToolRecipes(
                    output = output,
                    group = "matrix_coal_suit",
                    material = Items.COAL_BLOCK,
                    sword = CoalSwordItem,
                    pickaxe = CoalPickaxeItem,
                    axe = CoalAxeItem,
                    shovel = CoalShovelItem,
                    hoe = CoalHoeItem,
                )
            }

            private fun buildStoneSuitRecipes(output: RecipeOutput) {
                buildArmorRecipes(
                    output = output,
                    group = "matrix_stone_suit",
                    material = Items.COBBLESTONE,
                    helmet = StoneHelmetItem,
                    chestplate = StoneChestplateItem,
                    leggings = StoneLeggingsItem,
                    boots = StoneBootsItem,
                )
            }

            private fun buildWoodenSuitRecipes(output: RecipeOutput) {
                buildArmorRecipes(
                    output = output,
                    group = "matrix_wooden_suit",
                    materialTag = ItemTags.PLANKS,
                    unlockName = "has_planks",
                    helmet = WoodenHelmetItem,
                    chestplate = WoodenChestplateItem,
                    leggings = WoodenLeggingsItem,
                    boots = WoodenBootsItem,
                )
            }

            private fun buildArmorRecipes(
                output: RecipeOutput,
                group: String,
                material: ItemLike,
                helmet: ItemLike,
                chestplate: ItemLike,
                leggings: ItemLike,
                boots: ItemLike,
            ) {
                shaped(RecipeCategory.COMBAT, helmet)
                    .helmetPattern(material)
                    .saveWithItemUnlock(output, group, material)

                shaped(RecipeCategory.COMBAT, chestplate)
                    .chestplatePattern(material)
                    .saveWithItemUnlock(output, group, material)

                shaped(RecipeCategory.COMBAT, leggings)
                    .leggingsPattern(material)
                    .saveWithItemUnlock(output, group, material)

                shaped(RecipeCategory.COMBAT, boots)
                    .bootsPattern(material)
                    .saveWithItemUnlock(output, group, material)
            }

            private fun buildArmorRecipes(
                output: RecipeOutput,
                group: String,
                materialTag: TagKey<Item>,
                unlockName: String,
                helmet: ItemLike,
                chestplate: ItemLike,
                leggings: ItemLike,
                boots: ItemLike,
            ) {
                shaped(RecipeCategory.COMBAT, helmet)
                    .helmetPattern(materialTag)
                    .saveWithTagUnlock(output, group, unlockName, materialTag)

                shaped(RecipeCategory.COMBAT, chestplate)
                    .chestplatePattern(materialTag)
                    .saveWithTagUnlock(output, group, unlockName, materialTag)

                shaped(RecipeCategory.COMBAT, leggings)
                    .leggingsPattern(materialTag)
                    .saveWithTagUnlock(output, group, unlockName, materialTag)

                shaped(RecipeCategory.COMBAT, boots)
                    .bootsPattern(materialTag)
                    .saveWithTagUnlock(output, group, unlockName, materialTag)
            }

            private fun buildToolRecipes(
                output: RecipeOutput,
                group: String,
                material: ItemLike,
                sword: ItemLike,
                pickaxe: ItemLike,
                axe: ItemLike,
                shovel: ItemLike,
                hoe: ItemLike,
            ) {
                shaped(RecipeCategory.COMBAT, sword)
                    .swordPattern(material)
                    .saveWithItemUnlock(output, group, material)

                shaped(RecipeCategory.TOOLS, pickaxe)
                    .pickaxePattern(material)
                    .saveWithItemUnlock(output, group, material)

                shaped(RecipeCategory.TOOLS, axe)
                    .axePattern(material)
                    .saveWithItemUnlock(output, group, material)

                shaped(RecipeCategory.TOOLS, shovel)
                    .shovelPattern(material)
                    .saveWithItemUnlock(output, group, material)

                shaped(RecipeCategory.TOOLS, hoe)
                    .hoePattern(material)
                    .saveWithItemUnlock(output, group, material)
            }

            private fun ShapedRecipeBuilder.helmetPattern(material: ItemLike): ShapedRecipeBuilder {
                return pattern("###")
                    .pattern("# #")
                    .define('#', material)
            }

            private fun ShapedRecipeBuilder.helmetPattern(materialTag: TagKey<Item>): ShapedRecipeBuilder {
                return pattern("###")
                    .pattern("# #")
                    .define('#', materialTag)
            }

            private fun ShapedRecipeBuilder.chestplatePattern(material: ItemLike): ShapedRecipeBuilder {
                return pattern("# #")
                    .pattern("###")
                    .pattern("###")
                    .define('#', material)
            }

            private fun ShapedRecipeBuilder.chestplatePattern(materialTag: TagKey<Item>): ShapedRecipeBuilder {
                return pattern("# #")
                    .pattern("###")
                    .pattern("###")
                    .define('#', materialTag)
            }

            private fun ShapedRecipeBuilder.leggingsPattern(material: ItemLike): ShapedRecipeBuilder {
                return pattern("###")
                    .pattern("# #")
                    .pattern("# #")
                    .define('#', material)
            }

            private fun ShapedRecipeBuilder.leggingsPattern(materialTag: TagKey<Item>): ShapedRecipeBuilder {
                return pattern("###")
                    .pattern("# #")
                    .pattern("# #")
                    .define('#', materialTag)
            }

            private fun ShapedRecipeBuilder.bootsPattern(material: ItemLike): ShapedRecipeBuilder {
                return pattern("# #")
                    .pattern("# #")
                    .define('#', material)
            }

            private fun ShapedRecipeBuilder.bootsPattern(materialTag: TagKey<Item>): ShapedRecipeBuilder {
                return pattern("# #")
                    .pattern("# #")
                    .define('#', materialTag)
            }

            private fun ShapedRecipeBuilder.swordPattern(material: ItemLike,handle: Item = Items.STICK): ShapedRecipeBuilder {
                return pattern(" # ")
                    .pattern(" # ")
                    .pattern(" | ")
                    .define('#', material)
                    .define('|', handle)
            }

            private fun ShapedRecipeBuilder.pickaxePattern(material: ItemLike,handle: Item = Items.STICK): ShapedRecipeBuilder {
                return pattern("###")
                    .pattern(" | ")
                    .pattern(" | ")
                    .define('#', material)
                    .define('|', handle)
            }

            private fun ShapedRecipeBuilder.axePattern(material: ItemLike,handle: Item = Items.STICK): ShapedRecipeBuilder {
                return pattern("## ")
                    .pattern("#| ")
                    .pattern(" | ")
                    .define('#', material)
                    .define('|', handle)
            }

            private fun ShapedRecipeBuilder.shovelPattern(material: ItemLike,handle: Item = Items.STICK): ShapedRecipeBuilder {
                return pattern(" # ")
                    .pattern(" | ")
                    .pattern(" | ")
                    .define('#', material)
                    .define('|', handle)
            }

            private fun ShapedRecipeBuilder.hoePattern(material: ItemLike, handle: Item = Items.STICK): ShapedRecipeBuilder {
                return pattern("## ")
                    .pattern(" | ")
                    .pattern(" | ")
                    .define('#', material)
                    .define('|', handle)
            }

            private fun ShapedRecipeBuilder.saveWithItemUnlock(output: RecipeOutput,group: String,material: ItemLike, ) {
                group(group)
                    .unlockedBy(getHasName(material), has(material))
                    .save(output)
            }

            private fun ShapedRecipeBuilder.saveWithTagUnlock(
                output: RecipeOutput,
                group: String,
                unlockName: String,
                materialTag: TagKey<Item>,
            ) {
                group(group)
                    .unlockedBy(unlockName, has(materialTag))
                    .save(output)
            }
        }
    }

    override fun getName(): String {
        return "MatrixRecipeProvider"
    }
}