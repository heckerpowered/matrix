/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.core

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
import heckerpowered.matrix.common.magic.channel.*
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getOrCreateChannelQueue
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.resource.Mana.Companion.plus
import heckerpowered.matrix.common.magic.spell.MemoryWipeMagic
import heckerpowered.matrix.common.persistent.queueSize
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.core.common.balance.*
import heckerpowered.matrix.core.inverseLerp
import heckerpowered.matrix.core.isInfiniteMana
import heckerpowered.matrix.core.lerp
import heckerpowered.matrix.core.mana
import heckerpowered.matrix.core.utility.EntitySearch.getNearestEntities
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry
import kotlin.math.round

/**
 * Represents a castable magic definition with channeling-based execution semantics.
 *
 * A magic is executed in two distinct phases:
 *
 * 1. Channeling
 *    The magic is enqueued into a target-specific channel queue and progresses
 *    over time. During this phase, no final effect is applied, but various
 *    real-time side effects and queue mutations may occur.
 *
 * 2. Casting
 *    The magic completes its channeling and applies its actual effect.
 *
 * Multiple magics may be queued on the same target. When this happens, magics
 * are processed sequentially according to their channel order. The collection
 * of magics associated with a single caster–target pair is referred to as a
 * channeling sequence.
 *
 * ### Calculation Model
 *
 * Most numeric properties of a magic (mana cost, channel time, etc.) follow a
 * three-layer calculation model:
 *
 * - NormalXXX: The immutable baseline defined by the magic itself.
 * - BaseXXX:  The calculation baseline for the current context, which may vary
 *             depending on state and rules.
 * - XXX:      The final effective value after all modifiers and calculations.
 *
 * This distinction allows the system to reason about relative changes (e.g.
 * increased or reduced cost) without mutating the true baseline.
 *
 * ### Queue Semantics
 *
 * For any queue-related calculation:
 *
 * - If the channel queue is {@code null}, it is treated as an empty queue that
 *   has not yet been created.
 * - A {@code null} queue never implies a locked or full state.
 *
 * ### State Expectations
 *
 * - Calculation-related methods must tolerate incomplete contexts.
 * - Channeling and casting methods assume a committed invocation and may throw
 *   if required state (e.g. an active player) is missing.
 *
 * @see ChannelQueue
 * @see MagicInvocation
 * @see MagicCalculationContext
 */
abstract class Magic(val definition: MagicDefinition) {
    companion object {
        const val DEFAULT_BLOOD_PACT_CONVERT_RATIO = 2.0

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

    open fun cast(invocation: MagicInvocation) {
        val caster = invocation.caster
        val target = invocation.target
        val payload = invocation.payload

        val player = caster.asPlayerOrNull() ?: return

        if (!player.isBloodPactActive) return
        if (player.wizardHelmet.getEnchantmentLevel(MANA_OVERFLOW_ENCHANTMENT_KEY) < 5) return
        if (payload.isSpread) return
        if ((0..100).random() > 50) return

        val nearestEntity = target.getNearestEntities(20.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != player && it.isAlive }
            .firstOrNull { player.getChannelQueue(it)?.isEmpty ?: true }
            ?: return

        val spreadInvocation = MagicInvocation(
            caster = invocation.caster,
            target = nearestEntity,
            queue = nearestEntity.getOrCreateChannelQueue(player),
            payload = ExecutionPayload(isSpread = true)
        )

        ChannelExecutor.channel(this, spreadInvocation)
    }

    /**
     * Invoked when this magic enters the channeling phase.
     *
     * This method is called immediately after a channel entry is enqueued, but
     * before any channeling progress has advanced. At this point, the magic is
     * considered committed to the channel queue, yet its final effect has not
     * been applied.
     *
     * This hook may:
     * - Mutate the channel queue state.
     * - Apply real-time channel-start side effects.
     * - Annotate or adjust the execution payload.
     *
     * This hook must not:
     * - Consume mana or health.
     * - Apply the final magic effect.
     *
     * @param invocation immutable invocation describing the committed channeling
     *                   context.
     * @throws IllegalStateException if the invocation does not resolve to an active
     *                               player-backed caster.
     */
    open fun channel(invocation: MagicInvocation) {
        val player = invocation.caster.entityOrNull() as PlayerEntity
        val queue = invocation.queue
        val payload = invocation.payload

        // Queue Mastery: The last magic to fill a queue has -50% mana cost and
        // locks the queue until all magics have channeled.
        val queueMasteryLevel = player.wizardHelmet.getEnchantmentLevel(QUEUE_MASTERY_ENCHANTMENT_KEY)
        val queuedMagicCount = queue.queuedMagicCount
        val queueSizeFull = player.queueSize
        if (queueMasteryLevel > 0 && queuedMagicCount == queueSizeFull) {
            queue.isLocked = true
        }

        if (player.isBloodPactActive) {
            WitherArmorChargedEffect.onEntityTick(player)
            val peakOverDriveLevel = player.wizardHelmet.getEnchantmentLevel(PEAK_OVERDRIVE_ENCHANTMENT_KEY)
            if (peakOverDriveLevel > 0) {
                channelPeakOverdrive(invocation)
            }
        }

        if (queue.contains<MemoryWipeMagic>()) {
            payload.isSpoofed = true
        }
    }

    /**
     * Triggers Peak Overdrive effects such as increasing load on wizard helmet.
     */
    protected open fun channelPeakOverdrive(invocation: MagicInvocation) {
        val player = invocation.caster.asPlayerOrNull()!!
        val currentLoad = player.wizardHelmet.getOrDefault(MatrixComponents.LOAD, .0)
        player.wizardHelmet.set(MatrixComponents.LOAD, currentLoad + 1)
    }

    /**
     * Determines whether this magic can be channeled under the given calculation
     * context.
     *
     * This method performs a non-committing availability check and must not mutate
     * any game state. It is intended for prediction, validation, and UI feedback.
     *
     * Queue-related rules follow these semantics:
     * - If the channel queue is {@code null}, it is treated as empty and unlocked.
     *
     * @param context calculation context describing the potential caster, target,
     *                queue state, and execution payload.
     * @return the availability status, or a concrete reason why the magic cannot
     *         be channeled.
     */
    open fun availableStatus(context: MagicCalculationContext): MagicAvailableStatus {
        if (!checkMana(context)) {
            return MagicAvailableStatus.AVAILABLE_MANA_NOT_ENOUGH
        }
        if (context.target == null && !mayChannelWithoutTarget(context)) {
            return MagicAvailableStatus.TARGET_MISSING
        }
        if (checkChannelQueueIsFull(context)) {
            return MagicAvailableStatus.CHANNEL_QUEUE_FULL
        }
        if (checkChannelQueueIsLocked(context)) {
            return MagicAvailableStatus.CHANNEL_QUEUE_LOCKED
        }

        return MagicAvailableStatus.AVAILABLE
    }

    /**
     * @return true if the sequence is locked.
     */
    protected open fun checkChannelQueueIsLocked(context: MagicCalculationContext): Boolean {
        return context.queue?.isLocked ?: false
    }

    /**
     * @return true if the player's channel sequence is full.
     */
    protected open fun checkChannelQueueIsFull(context: MagicCalculationContext): Boolean {
        val player = context.playerOrNull() ?: return false
        val queue = context.queue ?: return false
        return queue.isChanneling && queue.queuedMagicCount >= player.queueSize
    }

    /**
     * Whether the magic can be channeled without a target.
     */
    protected open fun mayChannelWithoutTarget(context: MagicCalculationContext): Boolean = false

    /**
     * Checks whether the caster has sufficient equivalent resources to afford
     * channeling this magic under the given context.
     *
     * This check is purely evaluative and does not perform any resource mutation.
     * All convertible resources (e.g. health via Blood Pact) are considered according
     * to the current rules.
     *
     * If the context does not resolve to a valid player-backed caster, this method
     * returns {@code false}.
     *
     * @param context calculation context used to evaluate affordability.
     * @return {@code true} if the magic can be afforded; {@code false} otherwise.
     */
    protected open fun checkMana(context: MagicCalculationContext): Boolean {
        val player = context.caster?.entityOrNull() as? PlayerEntity ?: return false
        if (player.isInfiniteMana) return true

        var mana = player.mana
        val cost = getCost(context)
        if (player.isBloodPactActive) {
            val convertRatio = getBloodPactConvertRatio(context)
            mana += (player.health * convertRatio).mana
        }

        return cost <= mana.amount
    }

    /**
     * Gets the base mana cost of this magic; this value is not necessarily the value needed to channel the magic,
     * but can be used to compare whether the mana required has increased or decreased.
     */
    fun getNormalCost(): Long = definition.baseCost.amount.toLong()

    open fun getBaseCost(context: MagicCalculationContext): Long {
        return getNormalCost()
    }

    open fun getMagicResistance(context: MagicCalculationContext): Double {
        val target = context.target
        if (target?.name?.string == "hecker") {
            return 4.0
        }
        return target?.adjustedManaResistance ?: .0
    }

    /**
     * Calculates the final mana cost required to channel this magic.
     *
     * This value is derived from:
     * - The base calculation cost for the current context.
     * - All applicable cost modifiers.
     * - Magic resistance effects.
     *
     * If the calculation context does not resolve to a valid player, the base cost
     * is returned without further modification.
     *
     * @param context calculation context describing the caster, target, queue state,
     *                and execution payload.
     * @return the final mana cost required to channel this magic.
     */
    open fun getCost(context: MagicCalculationContext): Long {
        val player = context.playerOrNull() ?: return getBaseCost(context)

        val target = context.target
        val queue = context.queue
        val accumulator = context.accumulator

        val baseCost = getBaseCost(context).toDouble()

        var costReduction = 0.0

        val proximatePropagationLevel = player.wizardHelmet.getEnchantmentLevel(PROXIMATE_PROPAGATION_ENCHANTMENT_KEY)
        if (target != null && proximatePropagationLevel > 0) {
            val squaredDistance = player.squaredDistanceTo(target)
            val maxDistanceSquare = 12.0 * 12.0
            val minDistanceSquare = 4.0 * 4.0
            val lerpFactor = 1 - squaredDistance.inverseLerp(minDistanceSquare..maxDistanceSquare).coerceIn(0.0, 1.0)
            costReduction += lerpFactor.lerp(0.0..0.35)
        }

        // Queue Mastery: The last magic to fill a queue has -50% mana cost and
        // locks the queue until all magics have channeled.
        val queueMasteryLevel = player.wizardHelmet.getEnchantmentLevel(QUEUE_MASTERY_ENCHANTMENT_KEY)
        val queuedMagicCount = queue?.queuedMagicCount
        val queueSizeOneOffFull = player.queueSize - 1
        if (queueMasteryLevel > 0 && queuedMagicCount == queueSizeOneOffFull) {
            costReduction += 0.5
        }

        accumulator.pushCostReduction(costReduction)
        accumulator.pushMagicResistance(getMagicResistance(context))

        return round(CostCalculator.compute(baseCost, accumulator)).toLong()
    }

    /**
     * Gets the time it takes for the magic to channel, in ticks; this value is not necessarily the value needed to
     * channel the magic, but can be used to compare whether the channel time has increased or decreased.
     */
    fun getNormalChannelTime(): Long = definition.baseChannelTime.ticks

    open fun getBaseChannelTime(context: MagicCalculationContext): Long {
        return getNormalChannelTime()
    }

    /**
     * Calculates the final channeling time required for this magic.
     *
     * The result includes all applicable channel speed modifiers and resistance
     * effects. If the context does not resolve to a valid player, the base channel
     * time is returned.
     *
     * @param context calculation context describing the caster, target, queue state,
     *                and execution payload.
     * @return the final channeling time in ticks.
     */
    open fun getChannelTime(context: MagicCalculationContext): Long {
        val player = context.playerOrNull() ?: return getBaseChannelTime(context)
        val queue = context.queue
        val accumulator = context.accumulator

        val effectiveTime = getBaseChannelTime(context).toDouble()
        var channelSpeedBonus = 0.0

        // Magic Queue: +30% channel speed for the second magic in a queue.
        // Queue Acceleration: +60% channel speed for magics third or later in the queue.
        // Wrap Dancer: +100% channel speed.
        // Peak Overdrive: + 50% channel speed when blood pact is activated.
        if (player.wizardHelmet.getEnchantmentLevel(MAGIC_QUEUE_ENCHANTMENT_KEY) > 0 &&
            queue?.channelingMagicCount == 1
        ) {
            channelSpeedBonus += 0.3
        }
        if (player.wizardHelmet.getEnchantmentLevel(QUEUE_ACCELERATION_ENCHANTMENT_KEY) > 0 &&
            (queue?.channelingMagicCount?.toLong() ?: 0L) >= 2
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
        accumulator.pushMagicResistance(getMagicResistance(context))

        return round(ChannelTimeCalculator.compute(effectiveTime, accumulator)).toLong()
    }

    /**
     * @return Ratio used when converting health to mana during Blood Pact.
     */
    open fun getBloodPactConvertRatio(context: MagicCalculationContext): Double {
        val player = context.playerOrNull() ?: return DEFAULT_BLOOD_PACT_CONVERT_RATIO
        val helmet = player.wizardHelmet.item as? WizardHelmet ?: return DEFAULT_BLOOD_PACT_CONVERT_RATIO
        return helmet.getBloodPactConversionEfficiency(context)
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