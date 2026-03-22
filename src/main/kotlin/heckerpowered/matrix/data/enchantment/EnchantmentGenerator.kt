/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.data.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.bloodPact
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.brutalStrength
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.enchantmentKey
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.guaranteed
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.kineticThrow
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.lastStand
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.lightningStrike
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.magicQueue
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.magicShield
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.manaOverflow
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.manaRegeneration
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.peakOverdrive
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.proximatePropagation
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.queueAcceleration
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.queueMastery
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.revival
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.secondWind
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.witherArmor
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.wizardForce
import heckerpowered.matrix.common.item.MatrixItemTags
import heckerpowered.matrix.common.magic.system.MagicManager
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
            entries, witherArmor, Enchantment.builder(
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
            entries, guaranteed, Enchantment.builder(
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
            entries, lastStand, Enchantment.builder(
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
            entries, revival, Enchantment.builder(
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
            entries, secondWind, Enchantment.builder(
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
            entries, proximatePropagation, Enchantment.builder(
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
            entries, magicQueue, Enchantment.builder(
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
            entries, queueAcceleration, Enchantment.builder(
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
            entries, queueMastery, Enchantment.builder(
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
            entries, manaOverflow, Enchantment.builder(
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
            entries, manaRegeneration, Enchantment.builder(
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
            entries, wizardForce, Enchantment.builder(
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
            entries, bloodPact, Enchantment.builder(
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
            entries, magicShield, Enchantment.builder(
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
            entries, brutalStrength, Enchantment.builder(
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
            entries, peakOverdrive, Enchantment.builder(
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
            entries, lightningStrike, Enchantment.builder(
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
            entries, kineticThrow, Enchantment.builder(
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