/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.reference

import heckerpowered.matrix.Matrix
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item

object ModItemIds : Iterable<ResourceKey<Item>> {
    val wardenChestplate = create("warden_chestplate")

    val redstoneHelmet = create("redstone_helmet")
    val redstoneChestplate = create("redstone_chestplate")
    val redstoneLeggings = create("redstone_leggings")
    val redstoneBoots = create("redstone_boots")
    val redstoneSword = create("redstone_sword")
    val redstonePickaxe = create("redstone_pickaxe")
    val redstoneAxe = create("redstone_axe")
    val redstoneShovel = create("redstone_shovel")
    val redstoneHoe = create("redstone_hoe")

    val lapisLazuliHelmet = create("lapis_lazuli_helmet")
    val lapisLazuliChestplate = create("lapis_lazuli_chestplate")
    val lapisLazuliLeggings = create("lapis_lazuli_leggings")
    val lapisLazuliBoots = create("lapis_lazuli_boots")
    val lapisLazuliSword = create("lapis_lazuli_sword")
    val lapisLazuliPickaxe = create("lapis_lazuli_pickaxe")
    val lapisLazuliAxe = create("lapis_lazuli_axe")
    val lapisLazuliShovel = create("lapis_lazuli_shovel")
    val lapisLazuliHoe = create("lapis_lazuli_hoe")

    val emeraldHelmet = create("emerald_helmet")
    val emeraldChestplate = create("emerald_chestplate")
    val emeraldLeggings = create("emerald_leggings")
    val emeraldBoots = create("emerald_boots")
    val emeraldSword = create("emerald_sword")
    val emeraldPickaxe = create("emerald_pickaxe")
    val emeraldAxe = create("emerald_axe")
    val emeraldShovel = create("emerald_shovel")
    val emeraldHoe = create("emerald_hoe")

    val coalHelmet = create("coal_helmet")
    val coalChestplate = create("coal_chestplate")
    val coalLeggings = create("coal_leggings")
    val coalBoots = create("coal_boots")
    val coalSword = create("coal_sword")
    val coalPickaxe = create("coal_pickaxe")
    val coalAxe = create("coal_axe")
    val coalShovel = create("coal_shovel")
    val coalHoe = create("coal_hoe")

    val stoneHelmet = create("stone_helmet")
    val stoneChestplate = create("stone_chestplate")
    val stoneLeggings = create("stone_leggings")
    val stoneBoots = create("stone_boots")

    val woodenHelmet = create("wooden_helmet")
    val woodenChestplate = create("wooden_chestplate")
    val woodenLeggings = create("wooden_leggings")
    val woodenBoots = create("wooden_boots")

    val wizardHelmet1 = create("wizard_helmet_1")
    val wizardHelmetHacker = create("wizard_helmet_hacker")
    val wizardHelmet2 = create("wizard_helmet_2")
    val wizardHelmet3 = create("wizard_helmet_3")
    val wizardHelmet4 = create("wizard_helmet_4")
    val wizardHelmet5 = create("wizard_helmet_5")
    val wizardHelmet10 = create("wizard_helmet_10")
    val wizardHelmet13 = create("wizard_helmet_13")

    val lightningChestplateBorrowedTime = create("lightning_chestplate_borrowed_time")
    val magicTalisman = create("magic_talisman")

    val finderArrow = create("finder_arrow")
    val metaBow = create("meta_bow")

    val all = listOf(
        wardenChestplate,

        redstoneHelmet,
        redstoneChestplate,
        redstoneLeggings,
        redstoneBoots,
        redstoneSword,
        redstonePickaxe,
        redstoneAxe,
        redstoneShovel,
        redstoneHoe,

        lapisLazuliHelmet,
        lapisLazuliChestplate,
        lapisLazuliLeggings,
        lapisLazuliBoots,
        lapisLazuliSword,
        lapisLazuliPickaxe,
        lapisLazuliAxe,
        lapisLazuliShovel,
        lapisLazuliHoe,

        emeraldHelmet,
        emeraldChestplate,
        emeraldLeggings,
        emeraldBoots,
        emeraldSword,
        emeraldPickaxe,
        emeraldAxe,
        emeraldShovel,
        emeraldHoe,

        coalHelmet,
        coalChestplate,
        coalLeggings,
        coalBoots,
        coalSword,
        coalPickaxe,
        coalAxe,
        coalShovel,
        coalHoe,

        stoneHelmet,
        stoneChestplate,
        stoneLeggings,
        stoneBoots,

        woodenHelmet,
        woodenChestplate,
        woodenLeggings,
        woodenBoots,

        wizardHelmet1,
        wizardHelmetHacker,
        wizardHelmet2,
        wizardHelmet3,
        wizardHelmet4,
        wizardHelmet5,
        wizardHelmet10,
        wizardHelmet13,

        lightningChestplateBorrowedTime,
        magicTalisman,

        finderArrow,
        metaBow,
    )

    private fun create(name: String): ResourceKey<Item> {
        return ResourceKey.create(Registries.ITEM, Matrix.identifier(name))
    }

    override fun iterator(): Iterator<ResourceKey<Item>> {
        return all.iterator()
    }
}