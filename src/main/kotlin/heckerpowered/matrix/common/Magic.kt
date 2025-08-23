/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common

import heckerpowered.matrix.common.effect.WitherArmorChargedEffect
import heckerpowered.matrix.common.effect.bloodPactActive
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MAGIC_QUEUE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MANA_OVERFLOW_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PEAK_OVERDRIVE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PROXIMATE_PROPAGATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.QUEUE_ACCELERATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.QUEUE_MASTERY_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.getEnchantmentLevel
import heckerpowered.matrix.common.entity.attribute.MatrixEntityAttributes.adjustedManaResistance
import heckerpowered.matrix.common.item.MatrixComponents
import heckerpowered.matrix.common.item.WizardHelmet5
import heckerpowered.matrix.common.magics.MagicAvailableStatus
import heckerpowered.matrix.common.magics.MagicData
import heckerpowered.matrix.common.persistent.*
import heckerpowered.matrix.core.getNearestEntities
import heckerpowered.matrix.core.inverseLerp
import heckerpowered.matrix.core.lerp
import heckerpowered.matrix.core.mana
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import kotlin.math.floor

/**
 * Magic can be cast by players, targeting a single living entity, and casting requires channeling; when
 * channeling multiple magics on the same target, they are channeled in sequence. There are limited number
 * of magics that can be channeled at once, magics that channeled at the same time are called channeling sequence.
 *
 * The phase between the player uses the magic and when it takes effect is called "channeling", when a
 * magic takes effect, it is called "casting".
 *
 * @see ChannelSequence
 */
abstract class Magic(
    /** The display name of the magic. */
    val name: Text,

    /** The base mana cost of the magic. */
    private val cost: Long,

    /** The description of the magic, shown in tooltips or GUIs. */
    val description: Text,

    /** The base time (in ticks) it takes to channel the magic. */
    private val channelTime: Long = 10,
) {
    /**
     * Called when the magic is actually cast.
     *
     * @param player The player casting the magic, may be null.
     * @param target The living entity being targeted.
     * @param sequence The channel sequence involved.
     */
    open fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData = MagicData()) {
        if (player != null &&
            player.bloodPactActive &&
            player.wizardHelmet.getEnchantmentLevel(MANA_OVERFLOW_ENCHANTMENT_KEY) >= 5 &&
            !data.isSpread &&
            (0..100).random() <= 50
        ) {
            (target.getNearestEntities(20.0) {
                it is LivingEntity
                        && (it.getChannelSequence(player)?.channelingMagicCount() ?: 0) == 0
                        && it != player
                        && it.isAlive
            } as? LivingEntity)?.let {
                ChannelSequence.channelMagic(this, player, it, false, data = MagicData(isSpread = true))
            }
        }
    }

    /**
     * Called when the magic is channeled (but not yet cast).
     * Can trigger effects like enchantment bonuses or state updates.
     *
     * @param player The player channeling the magic.
     * @param target The target of the magic.
     * @param sequence The channel sequence being updated.
     */
    open fun channel(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence, data: MagicData = MagicData()) {
        // Queue Mastery: The last magic to fill a queue has -50% mana cost and
        // locks the queue until all magics have channeled.
        if (player.wizardHelmet.getEnchantmentLevel(QUEUE_MASTERY_ENCHANTMENT_KEY) > 0 &&
            sequence.channelingMagicCount().toLong() == player.queueSize
        ) {
            sequence.locked = true
        }

        if (player.bloodPactActive) {
            WitherArmorChargedEffect.onEntityTick(player)
            if (player.wizardHelmet.getEnchantmentLevel(PEAK_OVERDRIVE_ENCHANTMENT_KEY) > 0) {
                channelPeakOverdrive(player, target, sequence)
            }
        }
    }

    /**
     * Triggers Peak Overdrive effects such as increasing load on wizard helmet.
     */
    protected open fun channelPeakOverdrive(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence) {
        val currentLoad = player.wizardHelmet.getOrDefault(MatrixComponents.LOAD, .0)
        player.wizardHelmet.set(MatrixComponents.LOAD, currentLoad + 1)
    }

    open fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): MagicAvailableStatus {
        if (!checkMana(player, target, sequence)) {
            return MagicAvailableStatus.AVAILABLE_MANA_NOT_ENOUGH
        }
        if (target == null && !mayChannelWithoutTarget(player)) {
            return MagicAvailableStatus.TARGET_MISSING
        }
        if (checkChannelSequenceIsFull(player, target, sequence)) {
            return MagicAvailableStatus.CHANNEL_QUEUE_FULL
        }
        if (checkChannelSequenceIsLocked(player, target, sequence)) {
            return MagicAvailableStatus.CHANNEL_QUEUE_LOCKED
        }

        return MagicAvailableStatus.AVAILABLE
    }

    /**
     * @return true if the sequence is locked.
     */
    protected open fun checkChannelSequenceIsLocked(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): Boolean {
        return sequence?.locked ?: false
    }

    /**
     * @return true if the player's channel sequence is full.
     */
    protected open fun checkChannelSequenceIsFull(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): Boolean {
        return (sequence?.channelingMagicCount() ?: 0) >= player.queueSize
    }

    /**
     * Whether the magic can be channeled without a target.
     */
    protected open fun mayChannelWithoutTarget(player: PlayerEntity): Boolean = false

    /**
     * Determines if the player has enough mana (or health via Blood Pact) to channel the magic.
     * @return true if channeling is affordable.
     */
    protected open fun checkMana(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): Boolean {
        if (player is ServerPlayerEntity && player.isInfiniteMana) {
            return true
        }

        var mana = player.mana
        val cost = getCost(player, target, sequence)
        if (player.bloodPactActive) {
            val convertRatio = getBloodPactConvertRatio(player, target, sequence)
            mana += player.health * convertRatio
        }

        return cost <= mana
    }

    /**
     * Gets the base mana cost of this magic; this value is not necessarily the value needed to channel the magic,
     * but can be used to compare whether the mana required has increased or decreased.
     */
    fun getNormalCost(): Long {
        return cost
    }

    open fun getBaseCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?, data: MagicData = MagicData()): Long {
        return getNormalCost()
    }

    /**
     * Gets the mana needed to channel this magic.
     */
    open fun getCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?, data: MagicData = MagicData()): Long {
        val cost = getBaseCost(player, target, sequence, data).toDouble()
        var costMultiplier = 1.0
        costMultiplier += target?.adjustedManaResistance ?: .0

        val enchantment = player.world.registryManager
            .getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
            .getOrThrow(PROXIMATE_PROPAGATION_ENCHANTMENT_KEY)
        if (target != null && EnchantmentHelper.getLevel(enchantment, player.wizardHelmet) > 0) {
            val dist = player.squaredDistanceTo(target)
            val maxDistanceSquare = 12.0 * 12.0
            val minDistanceSquare = 4.0 * 4.0
            val lerpRatio = 1 - dist.inverseLerp(minDistanceSquare..maxDistanceSquare).coerceIn(0.0, 1.0)
            costMultiplier -= lerpRatio.lerp(0.0..0.35)
        }

        // Queue Mastery: The last magic to fill a queue has -50% mana cost and
        // locks the queue until all magics have channeled.
        if (player.wizardHelmet.getEnchantmentLevel(QUEUE_MASTERY_ENCHANTMENT_KEY) > 0 &&
            sequence?.channelingMagicCount()?.toLong() == player.queueSize - 1
        ) {
            costMultiplier -= 0.5
        }

        return floor(cost * costMultiplier).toLong().coerceAtLeast(0)
    }

    /**
     * Gets the time it takes for the magic to channel, in ticks; this value is not necessarily the value needed to
     * channel the magic, but can be used to compare whether the channel time has increased or decreased.
     */
    fun getNormalChannelTime(): Long = channelTime

    open fun getBaseChannelTime(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence?, data: MagicData = MagicData()): Long {
        return getNormalChannelTime()
    }

    /**
     * Calculates actual channel time based on player enchantments, helmet effects, and active states.
     * @return Time in ticks required to channel the magic.
     */
    open fun getChannelTime(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence?, data: MagicData = MagicData()): Long {
        var effectiveTime = getBaseChannelTime(player, target, sequence, data).toDouble()
        var channelSpeedBonus = 1.0

        // Magic Queue: +30% channel speed for the second magic in a queue.
        // Queue Acceleration: +60% channel speed for magics third or later in the queue.
        // Wrap Dancer: +100% channel speed.
        // Peak Overdrive: + 50% channel speed when blood pact is activated.
        if (player.wizardHelmet.getEnchantmentLevel(MAGIC_QUEUE_ENCHANTMENT_KEY) > 0 &&
            sequence?.channelingMagicCount() == 2
        ) {
            channelSpeedBonus += 0.3
        }
        if (player.wizardHelmet.getEnchantmentLevel(QUEUE_ACCELERATION_ENCHANTMENT_KEY) > 0 &&
            (sequence?.channelingMagicCount()?.toLong() ?: 0L) >= 3
        ) {
            channelSpeedBonus += 0.6
        }
        if (player.wizardHelmet.item is WizardHelmet5) {
            channelSpeedBonus += 1.0
        }
        if (player.wizardHelmet.getEnchantmentLevel(PEAK_OVERDRIVE_ENCHANTMENT_KEY) > 0 &&
            player.bloodPactActive
        ) {
            channelSpeedBonus += 0.5
        }

        effectiveTime /= channelSpeedBonus
        effectiveTime *= (1.0 + target.adjustedManaResistance)
        return floor(effectiveTime).toLong().coerceAtLeast(0)
    }

    /**
     * @return Ratio used when converting health to mana during Blood Pact.
     */
    open fun getBloodPactConvertRatio(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?, data: MagicData = MagicData()): Double {
        var ratio = 2.0

        // Peak Overdrive: + 100% health to mana conversion efficiency.
        if (player.wizardHelmet.getEnchantmentLevel(PEAK_OVERDRIVE_ENCHANTMENT_KEY) > 0 && player.bloodPactActive) {
            ratio += 1.0
        }

        return ratio
    }

    /**
     * Unique ID of the magic, derived from its name.
     */
    val id: Int
        get() = name.hashCode()
}

fun LivingEntity.isInvulnerableToEffect(effect: RegistryEntry<StatusEffect>): Boolean {
    if (!canHaveStatusEffect(StatusEffectInstance(effect, 0, 0))) {
        return true
    }

    if (this is EnderDragonEntity || this is WitherEntity) {
        return true
    }

    return false
}