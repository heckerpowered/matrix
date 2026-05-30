/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.data.enchantment

import heckerpowered.matrix.common.enchantment.ModEnchantments
import heckerpowered.matrix.common.enchantment.ModEnchantments.enchantmentKey
import heckerpowered.matrix.common.enchantment.ModEnchantments.kineticThrow
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
                context, ModEnchantments.witherArmor,
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
                context, ModEnchantments.guaranteed,
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
                context, ModEnchantments.lastStand,
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
                context, ModEnchantments.revival,
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
                context, ModEnchantments.secondWind,
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
                context, ModEnchantments.proximatePropagation,
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
                context, ModEnchantments.magicQueue,
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
                context, ModEnchantments.queueAcceleration,
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
                context, ModEnchantments.queueMastery,
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
                context, ModEnchantments.manaOverflow,
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
                context, ModEnchantments.manaRegeneration,
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
                context, ModEnchantments.wizardForce,
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
                context, ModEnchantments.bloodPact,
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
                context, ModEnchantments.magicShield,
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
                context, ModEnchantments.brutalStrength,
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
                context, ModEnchantments.peakOverdrive,
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
                context, ModEnchantments.lightningStrike,
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
                context, kineticThrow,
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