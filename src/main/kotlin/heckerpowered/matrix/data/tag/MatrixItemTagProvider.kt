/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.data.tag

import heckerpowered.matrix.common.item.MatrixItemTags
import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.item.allMatrixItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.*
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.ItemTags
import java.util.concurrent.CompletableFuture

class MatrixItemTagProvider(
    fabricDataOutput: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricTagProvider<Item>(fabricDataOutput, RegistryKeys.ITEM, registriesFuture) {

    private fun addArmorEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.ARMOR_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<ArmorItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addHeadArmorEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.HEAD_ARMOR_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<ArmorItem>()
            .filter { it.type == ArmorItem.Type.HELMET }
            .forEach { tagBuilder.add(it) }
    }

    private fun addChestArmorEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.CHEST_ARMOR_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<ArmorItem>()
            .filter { it.type == ArmorItem.Type.CHESTPLATE }
            .forEach { tagBuilder.add(it) }
    }

    private fun addLeggingsArmorEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.LEG_ARMOR_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<ArmorItem>()
            .filter { it.type == ArmorItem.Type.LEGGINGS }
            .forEach { tagBuilder.add(it) }
    }

    private fun addBootsArmorEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.FOOT_ARMOR_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<ArmorItem>()
            .filter { it.type == ArmorItem.Type.BOOTS }
            .forEach { tagBuilder.add(it) }
    }

    private fun addHeadArmor() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.HEAD_ARMOR)
        allMatrixItems
            .filterIsInstance<ArmorItem>()
            .filter { it.type == ArmorItem.Type.HELMET }
            .forEach { tagBuilder.add(it) }
    }

    private fun addChestArmor() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.CHEST_ARMOR)
        allMatrixItems
            .filterIsInstance<ArmorItem>()
            .filter { it.type == ArmorItem.Type.CHESTPLATE }
            .forEach { tagBuilder.add(it) }
    }

    private fun addLeggingsArmor() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.LEG_ARMOR)
        allMatrixItems
            .filterIsInstance<ArmorItem>()
            .filter { it.type == ArmorItem.Type.LEGGINGS }
            .forEach { tagBuilder.add(it) }
    }

    private fun addBootsArmor() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.FOOT_ARMOR)
        allMatrixItems
            .filterIsInstance<ArmorItem>()
            .filter { it.type == ArmorItem.Type.BOOTS }
            .forEach { tagBuilder.add(it) }
    }

    private fun addWeaponEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.WEAPON_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<SwordItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addMiningEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.MINING_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<MiningToolItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addDurabilityEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.DURABILITY_ENCHANTABLE)
        allMatrixItems
            .filter { it.components.contains(DataComponentTypes.MAX_DAMAGE) }
            .forEach { tagBuilder.add(it) }
    }

    private fun addVanishingEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.VANISHING_ENCHANTABLE)
        allMatrixItems
            .filter { it is ArmorItem || it is ToolItem || it is RangedWeaponItem || it is PlayerHeadItem || it is CompassItem }
            .forEach { tagBuilder.add(it) }
    }

    private fun addSwordEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.SWORD_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<SwordItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addBowEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.BOW_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<BowItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addCrossbowEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.CROSSBOW_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<CrossbowItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addFireAspectEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.FIRE_ASPECT_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<SwordItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addSharpWeaponEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.SHARP_WEAPON_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<SwordItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addMiningLootEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.MINING_LOOT_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<MiningToolItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addFishingEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.FISHING_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<FishingRodItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addTridentEnchantable() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.TRIDENT_ENCHANTABLE)
        allMatrixItems
            .filterIsInstance<TridentItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addSwords() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.SWORDS)
        allMatrixItems
            .filterIsInstance<SwordItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addPickaxes() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.PICKAXES)
        allMatrixItems
            .filterIsInstance<PickaxeItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addAxes() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.AXES)
        allMatrixItems
            .filterIsInstance<AxeItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addShovels() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.SHOVELS)
        allMatrixItems
            .filterIsInstance<ShovelItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addHoes() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.HOES)
        allMatrixItems
            .filterIsInstance<HoeItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addArrows() {
        val tagBuilder = getOrCreateTagBuilder(ItemTags.ARROWS)
        allMatrixItems
            .filterIsInstance<ArrowItem>()
            .forEach { tagBuilder.add(it) }
    }

    private fun addWitchHelmets() {
        val tagBuilder = getOrCreateTagBuilder(MatrixItemTags.wizardHelmetTag)
        allMatrixItems
            .filterIsInstance<WizardHelmet>()
            .forEach { tagBuilder.add(it) }
    }

    override fun configure(registries: RegistryWrapper.WrapperLookup) {
        addArmorEnchantable()
        addHeadArmorEnchantable()
        addChestArmorEnchantable()
        addLeggingsArmorEnchantable()
        addBootsArmorEnchantable()
        addHeadArmor()
        addChestArmor()
        addLeggingsArmor()
        addBootsArmor()
        addWeaponEnchantable()
        addMiningEnchantable()
        addDurabilityEnchantable()
        addVanishingEnchantable()
        addSwordEnchantable()
        addBowEnchantable()
        addCrossbowEnchantable()
        addFireAspectEnchantable()
        addSharpWeaponEnchantable()
        addMiningLootEnchantable()
        addFishingEnchantable()
        addTridentEnchantable()
        addSwords()
        addPickaxes()
        addAxes()
        addShovels()
        addHoes()
        addArrows()
        addWitchHelmets()
    }
}