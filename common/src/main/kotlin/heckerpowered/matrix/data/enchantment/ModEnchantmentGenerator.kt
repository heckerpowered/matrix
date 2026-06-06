/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.data.enchantment

import heckerpowered.matrix.common.enchantment.ModEnchantments
import heckerpowered.matrix.common.enchantment.ModEnchantments.KineticThrow
import heckerpowered.matrix.common.enchantment.ModEnchantments.enchantmentKey
import heckerpowered.matrix.common.item.ModItemTags
import heckerpowered.matrix.common.magic.system.Magics
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.item.enchantment.Enchantment
import java.util.concurrent.CompletableFuture


class ModEnchantmentGenerator(
    fabricPackOutput: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>,
) : FabricDynamicRegistryProvider(fabricPackOutput, registriesFuture) {
    override fun configure(registries: HolderLookup.Provider, entries: Entries) {
        entries.addAll(registries.lookupOrThrow(Registries.ENCHANTMENT))
    }

    override fun getName(): String {
        return "Enchantments"
    }

    companion object {
        fun bootstrap(context: BootstrapContext<Enchantment>) {
            // Wither Armor
            register(
                context, ModEnchantments.WitherArmor,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.CHEST
                    )
                )
            )

            // Guaranteed
            register(
                context, ModEnchantments.Guaranteed,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HAND
                    )
                )
            )

            // Last Stand
            register(
                context, ModEnchantments.LastStand,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HAND
                    )
                )
            )

            // Revival
            register(
                context, ModEnchantments.Revival,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.ARMOR
                    )
                )
            )

            // Second Wind
            register(
                context, ModEnchantments.SecondWind,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.ARMOR
                    )
                )
            )

            // Magic enchantments
            for (magic in Magics) {
                register(
                    context, magic.enchantmentKey,
                    Enchantment.enchantment(
                        Enchantment.definition(
                            context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                            10,
                            1,
                            Enchantment.dynamicCost(1, 10),
                            Enchantment.dynamicCost(1, 15),
                            5,
                            EquipmentSlotGroup.HEAD
                        )
                    )
                )
            }

            // Proximate Propagation
            register(
                context, ModEnchantments.ProximatePropagation,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                        10,
                        1,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HEAD
                    )
                )
            )

            // Magic Queue
            register(
                context, ModEnchantments.MagicQueue,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                        10,
                        1,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HEAD
                    )
                )
            )

            // Queue Acceleration
            register(
                context, ModEnchantments.QueueAcceleration,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                        10,
                        1,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HEAD
                    )
                )
            )

            // Queue Mastery
            register(
                context, ModEnchantments.QueueMastery,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                        10,
                        1,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HEAD
                    )
                )
            )

            // Mana Overflow
            register(
                context, ModEnchantments.ManaOverflow,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HEAD
                    )
                )
            )

            // Mana Regeneration
            register(
                context, ModEnchantments.ManaRegeneration,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HEAD
                    )
                )
            )

            // Wizard Force
            register(
                context, ModEnchantments.WizardForce,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HEAD
                    )
                )
            )

            // Blood Pact
            register(
                context, ModEnchantments.BloodPact,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                        10,
                        1,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HEAD
                    )
                )
            )

            // Magic Shield
            register(
                context, ModEnchantments.MagicShield,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HEAD
                    )
                )
            )

            // Brutal Strength
            register(
                context, ModEnchantments.BrutalStrength,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HEAD
                    )
                )
            )

            // Peak Overdrive
            register(
                context, ModEnchantments.PeakOverdrive,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ModItemTags.wizardHelmetTag),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HEAD
                    )
                )
            )

            // Lightning Strike
            register(
                context, ModEnchantments.LightningStrike,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HAND
                    )
                )
            )

            // Kinetic Throw
            register(
                context, KineticThrow,
                Enchantment.enchantment(
                    Enchantment.definition(
                        context.lookup(Registries.ITEM).getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        10,
                        5,
                        Enchantment.dynamicCost(1, 10),
                        Enchantment.dynamicCost(1, 15),
                        5,
                        EquipmentSlotGroup.HAND
                    )
                )
            )
        }

        private fun register(context: BootstrapContext<Enchantment>, key: ResourceKey<Enchantment>, builder: Enchantment.Builder) {
            context.register(key, builder.build(key.identifier()))
        }
    }
}