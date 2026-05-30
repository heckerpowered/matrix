/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.item.ModItems.register
import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item

object ModItems : Iterable<Item> {
    val all = listOf<Item>(
        WardenChestplateItem,
        RedstoneHelmetItem,
        RedstoneChestplateItem,
        RedstoneLeggingsItem,
        RedstoneBootsItem,
        RedstoneSwordItem,
        RedstonePickaxeItem,
        RedstoneAxeItem,
        RedstoneShovelItem,
        RedstoneHoeItem,
        LapisLazuliHelmetItem,
        LapisLazuliChestplateItem,
        LapisLazuliLeggingsItem,
        LapisLazuliBootsItem,
        LapisLazuliSwordItem,
        LapisLazuliPickaxeItem,
        LapisLazuliAxeItem,
        LapisLazuliShovelItem,
        LapisLazuliHoeItem,

        EmeraldHelmetItem,
        EmeraldChestplateItem,
        EmeraldLeggingsItem,
        EmeraldBootsItem,
        EmeraldSwordItem,
        EmeraldPickaxeItem,
        EmeraldAxeItem,
        EmeraldShovelItem,
        EmeraldHoeItem,

        CoalHelmetItem,
        CoalChestplateItem,
        CoalLeggingsItem,
        CoalBootsItem,
        CoalSwordItem,
        CoalPickaxeItem,
        CoalAxeItem,
        CoalShovelItem,
        CoalHoeItem,

        StoneHelmetItem,
        StoneChestplateItem,
        StoneLeggingsItem,
        StoneBootsItem,

        WoodenHelmetItem,
        WoodenChestplateItem,
        WoodenLeggingsItem,
        WoodenBootsItem,

        WizardHelmetHacker,
        WizardHelmet1,
        WizardHelmet2,
        WizardHelmet3,
        WizardHelmet4,
        WizardHelmet5,
        WizardHelmet10,
        WizardHelmet13,

        LightningChestplate1,
        MagicTalismanItem,
        FinderArrowItem,
        MetaBowItem
    )

    private fun register(item: Item, name: String): Item {
        val identifier = Matrix.identifier(name)
        val registeredItem = Registry.register(BuiltInRegistries.ITEM, identifier, item)
        return registeredItem
    }

    private fun register(item: Item, key: ResourceKey<Item>): Item {
        val registeredItem = Registry.register(BuiltInRegistries.ITEM, key, item)
        return registeredItem
    }

    fun onInitialize() {
        register(WardenChestplateItem, ModItemIds.wardenChestplate)

        register(RedstoneHelmetItem, ModItemIds.redstoneHelmet)
        register(RedstoneChestplateItem, ModItemIds.redstoneChestplate)
        register(RedstoneLeggingsItem, ModItemIds.redstoneLeggings)
        register(RedstoneBootsItem, ModItemIds.redstoneBoots)
        register(RedstoneSwordItem, ModItemIds.redstoneSword)
        register(RedstonePickaxeItem, ModItemIds.redstonePickaxe)
        register(RedstoneAxeItem, ModItemIds.redstoneAxe)
        register(RedstoneShovelItem, ModItemIds.redstoneShovel)
        register(RedstoneHoeItem, ModItemIds.redstoneHoe)

        register(LapisLazuliHelmetItem, ModItemIds.lapisLazuliHelmet)
        register(LapisLazuliChestplateItem, ModItemIds.lapisLazuliChestplate)
        register(LapisLazuliLeggingsItem, ModItemIds.lapisLazuliLeggings)
        register(LapisLazuliBootsItem, ModItemIds.lapisLazuliBoots)
        register(LapisLazuliSwordItem, ModItemIds.lapisLazuliSword)
        register(LapisLazuliPickaxeItem, ModItemIds.lapisLazuliPickaxe)
        register(LapisLazuliAxeItem, ModItemIds.lapisLazuliAxe)
        register(LapisLazuliShovelItem, ModItemIds.lapisLazuliShovel)
        register(LapisLazuliHoeItem, ModItemIds.lapisLazuliHoe)

        register(EmeraldHelmetItem, ModItemIds.emeraldHelmet)
        register(EmeraldChestplateItem, ModItemIds.emeraldChestplate)
        register(EmeraldLeggingsItem, ModItemIds.emeraldLeggings)
        register(EmeraldBootsItem, ModItemIds.emeraldBoots)
        register(EmeraldSwordItem, ModItemIds.emeraldSword)
        register(EmeraldPickaxeItem, ModItemIds.emeraldPickaxe)
        register(EmeraldAxeItem, ModItemIds.emeraldAxe)
        register(EmeraldShovelItem, ModItemIds.emeraldShovel)
        register(EmeraldHoeItem, ModItemIds.emeraldHoe)

        register(CoalHelmetItem, ModItemIds.coalHelmet)
        register(CoalChestplateItem, ModItemIds.coalChestplate)
        register(CoalLeggingsItem, ModItemIds.coalLeggings)
        register(CoalBootsItem, ModItemIds.coalBoots)
        register(CoalSwordItem, ModItemIds.coalSword)
        register(CoalPickaxeItem, ModItemIds.coalPickaxe)
        register(CoalAxeItem, ModItemIds.coalAxe)
        register(CoalShovelItem, ModItemIds.coalShovel)
        register(CoalHoeItem, ModItemIds.coalHoe)

        register(StoneHelmetItem, ModItemIds.stoneHelmet)
        register(StoneChestplateItem, ModItemIds.stoneChestplate)
        register(StoneLeggingsItem, ModItemIds.stoneLeggings)
        register(StoneBootsItem, ModItemIds.stoneBoots)

        register(WoodenHelmetItem, ModItemIds.woodenHelmet)
        register(WoodenChestplateItem, ModItemIds.woodenChestplate)
        register(WoodenLeggingsItem, ModItemIds.woodenLeggings)
        register(WoodenBootsItem, ModItemIds.woodenBoots)

        register(WizardHelmet1, ModItemIds.wizardHelmet1)
        register(WizardHelmetHacker, ModItemIds.wizardHelmetHacker)
        register(WizardHelmet2, ModItemIds.wizardHelmet2)
        register(WizardHelmet3, ModItemIds.wizardHelmet3)
        register(WizardHelmet4, ModItemIds.wizardHelmet4)
        register(WizardHelmet5, ModItemIds.wizardHelmet5)
        register(WizardHelmet10, ModItemIds.wizardHelmet10)
        register(WizardHelmet13, ModItemIds.wizardHelmet13)

        register(LightningChestplate1, ModItemIds.lightningChestplateBorrowedTime)
        register(MagicTalismanItem, ModItemIds.magicTalisman)

        register(FinderArrowItem, ModItemIds.finderArrow)
        register(MetaBowItem, ModItemIds.metaBow)
    }

    override fun iterator(): Iterator<Item> {
        return all.iterator()
    }
}