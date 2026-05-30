/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.core.Magic
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemInstance
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.Level
import kotlin.jvm.optionals.getOrNull

object ModEnchantments {
    @JvmField
    val witherArmor = createId("wither_armor")

    @JvmField
    val guaranteed = createId("guaranteed")

    @JvmField
    val lastStand = createId("last_stand")

    @JvmField
    val revival = createId("revival")

    @JvmField
    val secondWind = createId("second_wind")

    @JvmField
    val proximatePropagation = createId("proximate_propagation")

    @JvmField
    val magicQueue = createId("magic_queue")

    @JvmField
    val queueAcceleration = createId("queue_acceleration")

    @JvmField
    val queueMastery = createId("queue_mastery")

    @JvmField
    val manaOverflow = createId("mana_overflow")

    @JvmField
    val manaRegeneration = createId("mana_regeneration")

    @JvmField
    val wizardForce = createId("wizard_force")

    @JvmField
    val bloodPact = createId("blood_pact")

    @JvmField
    val magicShield = createId("magic_shield")

    @JvmField
    val brutalStrength = createId("brutal_strength")

    @JvmField
    val peakOverdrive = createId("peak_overdrive")

    @JvmField
    val lightningStrike = createId("lightning_strike")

    @JvmField
    val kineticThrow = createId("kinetic_throw")

    fun createId(name: String): ResourceKey<Enchantment> {
        val identifier = Matrix.identifier(name)
        return ResourceKey.create(Registries.ENCHANTMENT, identifier)
    }

    val Magic.enchantmentKey: ResourceKey<Enchantment>
        get() = createId(definition.identifier.path)

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
        ManaRegenerationEnchantment.onInitialize()
    }
}

fun Level.getEnchantmentHolder(enchantment: ResourceKey<Enchantment>): Holder<Enchantment>? {
    val enchantmentRegistry = registryAccess()[Registries.ENCHANTMENT].getOrNull()?.value() ?: return null
    val enchantmentHolder = enchantmentRegistry[enchantment].getOrNull() ?: return null
    return enchantmentHolder
}

fun ItemInstance.getEnchantmentLevel(level: Level, enchantment: ResourceKey<Enchantment>): Int {
    val enchantmentHolder = level.getEnchantmentHolder(enchantment) ?: return 0
    return EnchantmentHelper.getItemEnchantmentLevel(enchantmentHolder, this)
}

fun LivingEntity.getEnchantmentLevel(enchantmentKey: ResourceKey<Enchantment>): Int {
    val enchantmentHolder = level().getEnchantmentHolder(enchantmentKey) ?: return 0
    return EnchantmentHelper.getEnchantmentLevel(enchantmentHolder, this)
}