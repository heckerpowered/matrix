/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.common.effect.WitherArmorChargedEffect
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MAGIC_QUEUE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MANA_OVERFLOW_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PEAK_OVERDRIVE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PROXIMATE_PROPAGATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.QUEUE_ACCELERATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.QUEUE_MASTERY_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.getEnchantmentLevel
import heckerpowered.matrix.common.entity.attribute.MatrixEntityAttributes.adjustedManaResistance
import heckerpowered.matrix.common.item.MatrixComponents
import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.item.WizardHelmet5
import heckerpowered.matrix.common.magic.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.magic.Mana.Companion.plus
import heckerpowered.matrix.common.persistent.isInfiniteMana
import heckerpowered.matrix.common.persistent.queueSize
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.core.common.balance.*
import heckerpowered.matrix.core.inverseLerp
import heckerpowered.matrix.core.lerp
import heckerpowered.matrix.core.mana
import heckerpowered.matrix.core.utility.EntitySearch.getNearestEntities
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
import kotlin.math.round

/**
 * Magic can be cast by players, targeting a single living entity, and casting requires channeling; when
 * channeling multiple magics on the same target, they are channeled in sequence. There are limited number
 * of magics that can be channeled at once, magics that channeled at the same time are called channeling sequence.
 *
 * The phase between the player uses the magic and when it takes effect is called "channeling", when a
 * magic takes effect, it is called "casting".
 *
 * @see ChannelQueue
 */
abstract class Magic(val definition: MagicDefinition) {
    companion object {
        private const val CHANNEL_SPEED_LANE_NAME = "channel_speed"
        private const val MAGIC_RESISTANCE_LANE_NAME = "magic_resistance"
        private const val COST_REDUCTION_LANE_NAME = "cost_reduction"

        /**
         * ChannelTime = EffectiveTime / (1.0 + ChannelSpeed) * (1.0 + MagicResistance)
         */
        private val ChannelTimeCalculationPlan = CalculationPlan(
            Lane(
                CHANNEL_SPEED_LANE_NAME,
                SumOperator,
            ) { currentValue, laneValue -> currentValue / (1.0 + laneValue) },
            Lane(
                MAGIC_RESISTANCE_LANE_NAME,
                SumOperator
            ) { currentValue, laneValue -> currentValue * (1.0 + laneValue) }
        ) { it.coerceAtLeast(.0) }

        /**
         * Cost = BaseCost * (1.0 - CostReduction) * (1.0 + MagicResistance)
         */
        private val CostCalculationPlan = CalculationPlan(
            Lane(
                COST_REDUCTION_LANE_NAME,
                SumOperator,
            ) { currentValue, laneValue -> currentValue * (1.0 - laneValue) },
            Lane(
                MAGIC_RESISTANCE_LANE_NAME,
                SumOperator
            ) { currentValue, laneValue -> currentValue * (1.0 + laneValue) }
        ) { it.coerceAtLeast(.0) }

        val ChannelTimeCalculator = NumericCalculator(ChannelTimeCalculationPlan)
        val CostCalculator = NumericCalculator(CostCalculationPlan)

        fun Accumulator.pushChannelSpeed(value: Double) = push(CHANNEL_SPEED_LANE_NAME, value)
        fun Accumulator.pushMagicResistance(value: Double) = push(MAGIC_RESISTANCE_LANE_NAME, value)
        fun Accumulator.pushCostReduction(value: Double) = push(COST_REDUCTION_LANE_NAME, value)
    }

    /**
     * Called when the magic is actually cast.
     *
     * @param player The player casting the magic, may be null.
     * @param target The living entity being targeted.
     * @param sequence The channel sequence involved.
     */
    open fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: ExecutionPayload = ExecutionPayload()) {
        if (player != null &&
            player.isBloodPactActive &&
            player.wizardHelmet.getEnchantmentLevel(MANA_OVERFLOW_ENCHANTMENT_KEY) >= 5 &&
            !data.isSpread &&
            (0..100).random() <= 50
        ) {
            val nearestEntity = target.getNearestEntities(20.0)
                .filterIsInstance<LivingEntity>()
                .filter { it != player && it.isAlive }
                .firstOrNull { player.getChannelQueue(it)?.isEmpty ?: true }
            if (nearestEntity != null) {
                ChannelExecutor.channel(this, player, nearestEntity, ChannelPlan(data = ExecutionPayload(isSpread = true)))
            }
        }
    }

    /**
     * Called immediately after the player uses this magic to begin channeling.
     *
     * At this point the magic is either already being channeled or has just
     * been added to the player's current [ChannelQueue]. This hook can be used
     * to apply real-time effects such as enchantment bonuses, state updates,
     * or incremental charge mechanics.
     *
     * The magic has not yet been cast — its effect will trigger later when
     * channeling completes and [cast] is invoked.
     *
     * @param player the player who initiated or continues channeling this magic
     * @param target the entity currently targeted by the channeling
     * @param queue the channel queue this magic belongs to
     * @param data contextual magic data associated with this channel
     */
    open fun channel(player: PlayerEntity, target: LivingEntity, queue: ChannelQueue, data: ExecutionPayload = ExecutionPayload()) {
        // Queue Mastery: The last magic to fill a queue has -50% mana cost and
        // locks the queue until all magics have channeled.
        if (player.wizardHelmet.getEnchantmentLevel(QUEUE_MASTERY_ENCHANTMENT_KEY) > 0 &&
            queue.queue.size == player.queueSize.toInt()
        ) {
            queue.isLocked = true
        }

        if (player.isBloodPactActive) {
            WitherArmorChargedEffect.onEntityTick(player)
            if (player.wizardHelmet.getEnchantmentLevel(PEAK_OVERDRIVE_ENCHANTMENT_KEY) > 0) {
                channelPeakOverdrive(player, target, queue)
            }
        }

        if (queue.contains<MemoryWipeMagic>()) {
            data.isSpoofed = true
        }
    }

    /**
     * Triggers Peak Overdrive effects such as increasing load on wizard helmet.
     */
    protected open fun channelPeakOverdrive(player: PlayerEntity, target: LivingEntity, sequence: ChannelQueue) {
        val currentLoad = player.wizardHelmet.getOrDefault(MatrixComponents.LOAD, .0)
        player.wizardHelmet.set(MatrixComponents.LOAD, currentLoad + 1)
    }

    open fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?): MagicAvailableStatus {
        if (!checkMana(player, target, sequence)) {
            return MagicAvailableStatus.AVAILABLE_MANA_NOT_ENOUGH
        }
        if (target == null && !mayChannelWithoutTarget(player)) {
            return MagicAvailableStatus.TARGET_MISSING
        }
        if (checkChannelQueueIsFull(player, target, sequence)) {
            return MagicAvailableStatus.CHANNEL_QUEUE_FULL
        }
        if (checkChannelQueueIsLocked(player, target, sequence)) {
            return MagicAvailableStatus.CHANNEL_QUEUE_LOCKED
        }

        return MagicAvailableStatus.AVAILABLE
    }

    /**
     * @return true if the sequence is locked.
     */
    protected open fun checkChannelQueueIsLocked(player: PlayerEntity, target: LivingEntity?, queue: ChannelQueue?): Boolean {
        return queue?.isLocked ?: false
    }

    /**
     * @return true if the player's channel sequence is full.
     */
    protected open fun checkChannelQueueIsFull(player: PlayerEntity, target: LivingEntity?, queue: ChannelQueue?): Boolean {
        return queue != null &&
                queue.isChanneling &&
                queue.queue.size >= player.queueSize.toInt()
    }

    /**
     * Whether the magic can be channeled without a target.
     */
    protected open fun mayChannelWithoutTarget(player: PlayerEntity): Boolean = false

    /**
     * Determines if the player has enough mana (or health via Blood Pact) to channel the magic.
     * @return true if channeling is affordable.
     */
    protected open fun checkMana(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?): Boolean {
        if (player is ServerPlayerEntity && player.isInfiniteMana) {
            return true
        }

        var mana = player.mana
        val cost = getCost(player, target, sequence)
        if (player.isBloodPactActive) {
            val convertRatio = getBloodPactConvertRatio(player, target, sequence)
            mana += (player.health * convertRatio).mana
        }

        return cost <= mana.amount
    }

    /**
     * Gets the base mana cost of this magic; this value is not necessarily the value needed to channel the magic,
     * but can be used to compare whether the mana required has increased or decreased.
     */
    fun getNormalCost(): Long = definition.baseCost.amount.toLong()

    open fun getBaseCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?, data: ExecutionPayload = ExecutionPayload()): Long {
        return getNormalCost()
    }

    open fun getMagicResistance(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?, data: ExecutionPayload = ExecutionPayload()): Double {
        if (target?.name?.string == "hecker") {
            return 4.0
        }
        return target?.adjustedManaResistance ?: .0
    }

    /**
     * Gets the mana needed to channel this magic.
     */
    open fun getCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?, data: ExecutionPayload = ExecutionPayload(), accumulator: Accumulator = Accumulator()): Long {
        val cost = getBaseCost(player, target, sequence, data).toDouble()
        var costReduction = 0.0

        val enchantment = player.world.registryManager
            .getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
            .getOrThrow(PROXIMATE_PROPAGATION_ENCHANTMENT_KEY)
        if (target != null && EnchantmentHelper.getLevel(enchantment, player.wizardHelmet) > 0) {
            val dist = player.squaredDistanceTo(target)
            val maxDistanceSquare = 12.0 * 12.0
            val minDistanceSquare = 4.0 * 4.0
            val lerpRatio = 1 - dist.inverseLerp(minDistanceSquare..maxDistanceSquare).coerceIn(0.0, 1.0)
            costReduction += lerpRatio.lerp(0.0..0.35)
        }

        // Queue Mastery: The last magic to fill a queue has -50% mana cost and
        // locks the queue until all magics have channeled.
        if (player.wizardHelmet.getEnchantmentLevel(QUEUE_MASTERY_ENCHANTMENT_KEY) > 0 &&
            sequence?.queue?.size?.toLong() == player.queueSize - 1
        ) {
            costReduction += 0.5
        }

        accumulator.pushCostReduction(costReduction)
        accumulator.pushMagicResistance(getMagicResistance(player, target, sequence, data))

        return round(CostCalculator.compute(cost, accumulator)).toLong()
    }

    /**
     * Gets the time it takes for the magic to channel, in ticks; this value is not necessarily the value needed to
     * channel the magic, but can be used to compare whether the channel time has increased or decreased.
     */
    fun getNormalChannelTime(): Long = definition.baseChannelTime.ticks

    open fun getBaseChannelTime(player: PlayerEntity, target: LivingEntity, sequence: ChannelQueue?, data: ExecutionPayload = ExecutionPayload()): Long {
        return getNormalChannelTime()
    }

    /**
     * Calculates actual channel time based on player enchantments, helmet effects, and active states.
     * @return Time in ticks required to channel the magic.
     */
    open fun getChannelTime(player: PlayerEntity, target: LivingEntity, sequence: ChannelQueue?, data: ExecutionPayload = ExecutionPayload(), accumulator: Accumulator = Accumulator()): Long {
        val effectiveTime = getBaseChannelTime(player, target, sequence, data).toDouble()
        var channelSpeedBonus = 0.0

        // Magic Queue: +30% channel speed for the second magic in a queue.
        // Queue Acceleration: +60% channel speed for magics third or later in the queue.
        // Wrap Dancer: +100% channel speed.
        // Peak Overdrive: + 50% channel speed when blood pact is activated.
        if (player.wizardHelmet.getEnchantmentLevel(MAGIC_QUEUE_ENCHANTMENT_KEY) > 0 &&
            sequence?.channelingMagicCount == 1
        ) {
            channelSpeedBonus += 0.3
        }
        if (player.wizardHelmet.getEnchantmentLevel(QUEUE_ACCELERATION_ENCHANTMENT_KEY) > 0 &&
            (sequence?.channelingMagicCount?.toLong() ?: 0L) >= 2
        ) {
            channelSpeedBonus += 0.6
        }
        if (player.wizardHelmet.item is WizardHelmet5) {
            channelSpeedBonus += 1.0
        }
        if (player.wizardHelmet.getEnchantmentLevel(PEAK_OVERDRIVE_ENCHANTMENT_KEY) > 0 &&
            player.isBloodPactActive
        ) {
            channelSpeedBonus += 0.5
        }

        accumulator.pushChannelSpeed(channelSpeedBonus)
        accumulator.pushMagicResistance(getMagicResistance(player, target, sequence, data))

        return round(ChannelTimeCalculator.compute(effectiveTime, accumulator)).toLong()
    }

    /**
     * @return Ratio used when converting health to mana during Blood Pact.
     */
    open fun getBloodPactConvertRatio(player: PlayerEntity, target: LivingEntity?, queue: ChannelQueue?, data: ExecutionPayload = ExecutionPayload()): Double {
        return (player.wizardHelmet.item as? WizardHelmet)?.getBloodPactConversionEfficiency(player, target, queue, data) ?: 2.0
    }
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