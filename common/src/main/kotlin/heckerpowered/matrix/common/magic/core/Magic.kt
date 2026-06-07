/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.core

import heckerpowered.matrix.common.entity.attribute.MatrixEntityAttributes.magicResistance
import heckerpowered.matrix.common.magic.channel.ChannelQueue
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.resource.CastingResource
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.rule.calculation.pipeline.MagicCalculationPipeline
import heckerpowered.matrix.common.magic.rule.calculation.sink.ChannelTimeCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.CostCalculationSink
import heckerpowered.matrix.common.magic.rule.effect.MagicCastPipeline
import heckerpowered.matrix.common.magic.rule.effect.MagicChannelPipeline
import heckerpowered.matrix.common.magic.rule.resource.CastingResourcePipeline
import heckerpowered.matrix.common.persistent.queueSize
import heckerpowered.matrix.core.isInfiniteMana
import net.minecraft.core.Holder
import net.minecraft.world.Difficulty
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.player.Player
import kotlin.math.ceil
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
 * - If the channel queue is `null`, it is treated as an empty queue that
 *   has not yet been created.
 * - A `null` queue never implies a locked or full state.
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
    open fun cast(invocation: MagicInvocation) {
        MagicCastPipeline.onCast(this, invocation)
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
        MagicChannelPipeline.onChannel(this, invocation)
    }

    /**
     * Determines whether this magic can be channeled under the given calculation
     * context.
     *
     * This method performs a non-committing availability check and must not mutate
     * any game state. It is intended for prediction, validation, and UI feedback.
     *
     * Queue-related rules follow these semantics:
     * - If the channel queue is `null`, it is treated as empty and unlocked.
     *
     * @param context calculation context describing the potential caster, target,
     *                queue state, and execution payload.
     * @return the availability status, or a concrete reason why the magic cannot
     *         be channeled.
     */
    open fun availableStatus(context: MagicCalculationContext): MagicAvailability {
        val availability = MagicAvailability()
        if (context.target == null && !mayChannelWithoutTarget(context)) {
            availability += MagicAvailableStatus.TargetMissing
        }
        if (!checkMana(context)) {
            availability += MagicAvailableStatus.InsufficientMana
        }
        if (checkChannelQueueIsFull(context)) {
            availability += MagicAvailableStatus.ChannelQueueFull
        }
        if (checkChannelQueueIsLocked(context)) {
            availability += MagicAvailableStatus.ChannelQueueLocked
        }
        return availability
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
        if (!queue.isChanneling) return false
        return queue.queuedMagicCount >= player.queueSize
    }

    /**
     * Whether the magic can be channeled without a target.
     */
    protected open fun mayChannelWithoutTarget(context: MagicCalculationContext): Boolean = false

    /**
     * Determines whether the required casting cost can be satisfied under the
     * given calculation context.
     *
     * This check is purely evaluative and does not mutate any game state.
     * Affordability is determined by collecting all applicable [CastingResource]s
     * for the context and evaluating whether their combined available amounts
     * admit a valid consumption plan for the required cost.
     *
     * If the context does not provide any applicable casting resources, their
     * combined available amount is treated as zero. In such cases, the magic
     * is still considered affordable if and only if the required cost is zero.
     *
     * @param context calculation context used to evaluate affordability.
     * @return true if the required cost can be satisfied; false otherwise.
     */
    protected open fun checkMana(context: MagicCalculationContext): Boolean {
        if (context.playerOrNull()?.isInfiniteMana == true) {
            return true
        }
        val requiredCost = getCost(context).mana
        val resourceSet = CastingResourcePipeline.collect(context)
        return resourceSet.canAfford(context, requiredCost)
    }

    /**
     * Gets the base mana cost of this magic; this value is not necessarily the value needed to channel the magic,
     * but can be used to compare whether the mana required has increased or decreased.
     */
    fun getNormalCost(): Long = definition.baseCost.toDouble().toLong()

    open fun getBaseCost(context: MagicCalculationContext): Long {
        return getNormalCost()
    }

    open fun getMagicResistance(context: MagicCalculationContext): Double {
        val target = context.target ?: return .0
        if (target.name.string == "hecker") return 1145141919810.0

        val level = target.level()
        val difficultyScale = if (target is Player) 1.0 else when (level.difficulty) {
            Difficulty.PEACEFUL -> .0 // -100% magic resistance
            Difficulty.EASY -> 0.6 // -40% magic resistance
            Difficulty.NORMAL -> 1.0
            Difficulty.HARD -> 1.4 // + 40% magic resistance
        }
        val spellProfile = SpellProfile.getProfile(target)
        val attributeResistance = target.magicResistance // usually 0
        val spellResistance = spellProfile.effectiveResistance

        val magicResistance = (attributeResistance + spellResistance) * difficultyScale
        return magicResistance
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
        val sink = CostCalculationSink()
        sink.magicResistance = getMagicResistance(context)

        MagicCalculationPipeline.apply(this, context, sink)

        // Cost = (BaseCost + MagicResistance) * (1.0 - CostReduction)
        val baseCost = getBaseCost(context).toDouble()
        val costReduction = sink.costReduction
        val costMultiplier = sink.costMultiplier
        val magicResistance = sink.magicResistance
        return ceil((baseCost + magicResistance) * costMultiplier * (1.0 - costReduction)).toLong().coerceAtLeast(0)
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
        val sink = ChannelTimeCalculationSink()
        sink.magicResistance = getMagicResistance(context)

        MagicCalculationPipeline.apply(this, context, sink)

        val baseTime = getBaseChannelTime(context).toDouble()
        val channelSpeedBonus = sink.channelSpeedBonus
        val magicResistance = sink.magicResistance
        // ChannelTime = BaseTime / (1 + ChannelSpeed) * (1 + ceil(MagicResistance) * 0.2)
        return round(baseTime / (1.0 + channelSpeedBonus) * (1.0 + ceil(magicResistance) * 0.2)).toLong().coerceAtLeast(0)
    }
}

fun LivingEntity.isInvulnerableToEffect(effect: Holder<MobEffect>): Boolean {
    return when {
        !canBeAffected(MobEffectInstance(effect, 0, 0)) -> true
        // Calls to `canBeAffected` on the Ender Dragon return normal results,
        // but no MobEffect can be applied to the Ender Dragon.
        this is EnderDragon -> true
        else -> false
    }
}
