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
    val WITHER_ARMOR_ENCHANTMENT_KEY = of("wither_armor")

    @JvmField
    val GUARANTEED_ENCHANTMENT_KEY = of("guaranteed")

    @JvmField
    val LAST_STAND_ENCHANTMENT_KEY = of("last_stand")

    @JvmField
    val REVIVAL_ENCHANTMENT_KEY = of("revival")

    @JvmField
    val SECOND_WIND_ENCHANTMENT_KEY = of("second_wind")

    @JvmField
    val PROXIMATE_PROPAGATION_ENCHANTMENT_KEY = of("proximate_propagation")

    @JvmField
    val MAGIC_QUEUE_ENCHANTMENT_KEY = of("magic_queue")

    @JvmField
    val QUEUE_ACCELERATION_ENCHANTMENT_KEY = of("queue_acceleration")

    @JvmField
    val QUEUE_MASTERY_ENCHANTMENT_KEY = of("queue_mastery")

    @JvmField
    val MANA_OVERFLOW_ENCHANTMENT_KEY = of("mana_overflow")

    @JvmField
    val MANA_REGENERATION_ENCHANTMENT_KEY = of("mana_regeneration")

    @JvmField
    val WIZARD_FORCE_ENCHANTMENT_KEY = of("wizard_force")

    @JvmField
    val BLOOD_PACT_ENCHANTMENT_KEY = of("blood_pact")

    @JvmField
    val MAGIC_SHIELD_ENCHANTMENT_KEY = of("magic_shield")

    @JvmField
    val BRUTAL_STRENGTH_ENCHANTMENT_KEY = of("brutal_strength")

    @JvmField
    val PEAK_OVERDRIVE_ENCHANTMENT_KEY = of("peak_overdrive")

    @JvmField
    val LIGHTNING_STRIKE_ENCHANTMENT_KEY = of("lightning_strike")

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