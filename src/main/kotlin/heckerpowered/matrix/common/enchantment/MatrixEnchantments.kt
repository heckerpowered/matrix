/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.core.Magic
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

object MatrixEnchantments {
    @JvmField
    val witherArmor = of("wither_armor")

    @JvmField
    val guaranteed = of("guaranteed")

    @JvmField
    val lastStand = of("last_stand")

    @JvmField
    val revival = of("revival")

    @JvmField
    val secondWind = of("second_wind")

    @JvmField
    val proximatePropagation = of("proximate_propagation")

    @JvmField
    val magicQueue = of("magic_queue")

    @JvmField
    val queueAcceleration = of("queue_acceleration")

    @JvmField
    val queueMastery = of("queue_mastery")

    @JvmField
    val manaOverflow = of("mana_overflow")

    @JvmField
    val manaRegeneration = of("mana_regeneration")

    @JvmField
    val wizardForce = of("wizard_force")

    @JvmField
    val bloodPact = of("blood_pact")

    @JvmField
    val magicShield = of("magic_shield")

    @JvmField
    val brutalStrength = of("brutal_strength")

    @JvmField
    val peakOverdrive = of("peak_overdrive")

    @JvmField
    val lightningStrike = of("lightning_strike")

    @JvmField
    val kineticThrow = of("lightning_strike")

    fun LivingEntity.getEnchantmentLevel(registryKey: RegistryKey<Enchantment>): Int {
        val entry = world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(registryKey)
        return EnchantmentHelper.getEquipmentLevel(entry, this)
    }

    internal fun of(name: String): RegistryKey<Enchantment> {
        val identifier = Matrix.identifier(name)
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, identifier)
    }

    val Magic.enchantmentKey: RegistryKey<Enchantment>
        get() {
            return of(definition.identifier.path)
        }

    fun onInitialize() {
        WitherArmorEnchantment.onInitialize()
        GuaranteedEnchantment.onInitialize()
        LastStandEnchantment.onInitialize()
        RevivalEnchantment.onInitialize()
        SecondWindEnchantment.onInitialize()
        QueueMasteryEnchantment.onInitialize()
        WizardForceEnchantment.onInitialize()
        BrutalStrengthEnchantment.onInitialize()
        MagicShieldEnchantment.onInitialize()
        PeakOverdriveEnchantment.onInitialize()
        LightningStrikeEnchantment.onInitialize()
        ProximatePropagationEnchantment.onInitialize()
        MagicQueueEnchantment.onInitialize()
        QueueAccelerationEnchantment.onInitialize()
        ManaOverflowEnchantment.onInitialize()
    }

    fun ItemStack.getEnchantmentLevel(registryKey: RegistryKey<Enchantment>): Int {
        val entry = enchantments.enchantments.filter { !it.key.isEmpty }.find { it.key.get() == registryKey }
        if (entry == null) {
            return -1
        }

        return EnchantmentHelper.getLevel(entry, this)
    }
}