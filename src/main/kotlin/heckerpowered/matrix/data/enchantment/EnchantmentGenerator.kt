/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.data.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.BLOOD_PACT_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.BRUTAL_STRENGTH_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.GUARANTEED_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.LAST_STAND_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.LIGHTNING_STRIKE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MAGIC_QUEUE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MAGIC_SHIELD_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MANA_OVERFLOW_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MANA_REGENERATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PEAK_OVERDRIVE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PROXIMATE_PROPAGATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.QUEUE_ACCELERATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.QUEUE_MASTERY_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.REVIVAL_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.SECOND_WIND_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.WITHER_ARMOR_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.WIZARD_FORCE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.enchantmentKey
import heckerpowered.matrix.common.item.MatrixItemTags
import heckerpowered.matrix.common.magic.MagicManager
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition
import net.minecraft.component.type.AttributeModifierSlot
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.ItemTags
import java.util.concurrent.CompletableFuture

class EnchantmentGenerator(
    fabricDataOutput: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricDynamicRegistryProvider(fabricDataOutput, registriesFuture) {
    override fun getName(): String {
        return "MatrixEnchantmentGenerator"
    }

    override fun configure(registries: RegistryWrapper.WrapperLookup, entries: Entries) {
        register(
            entries, WITHER_ARMOR_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.BODY
                )
            )
        )
        register(
            entries, GUARANTEED_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HAND
                )
            )
        )
        register(
            entries, LAST_STAND_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HAND
                )
            )
        )
        register(
            entries, REVIVAL_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.ARMOR
                )
            )
        )
        register(
            entries, SECOND_WIND_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.ARMOR
                )
            )
        )

        for (magic in MagicManager.getRegisteredMagics()) {
            register(
                entries, magic.enchantmentKey, Enchantment.builder(
                    Enchantment.definition(
                        registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                        10,
                        1,
                        Enchantment.leveledCost(1, 10),
                        Enchantment.leveledCost(1, 15),
                        5,
                        AttributeModifierSlot.HEAD
                    )
                )
            )
        }
        register(
            entries, PROXIMATE_PROPAGATION_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, MAGIC_QUEUE_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, QUEUE_ACCELERATION_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, QUEUE_MASTERY_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, MANA_OVERFLOW_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, MANA_REGENERATION_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, WIZARD_FORCE_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, BLOOD_PACT_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, MAGIC_SHIELD_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, BRUTAL_STRENGTH_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, PEAK_OVERDRIVE_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, LIGHTNING_STRIKE_ENCHANTMENT_KEY, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
    }

    private fun register(
        entries: Entries,
        key: RegistryKey<Enchantment>,
        builder: Enchantment.Builder,
        vararg resourceConditions: ResourceCondition,
    ) {
        entries.add(key, builder.build(key.value), *resourceConditions)
    }
}