/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.channel

import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicAvailability
import heckerpowered.matrix.common.magic.core.MagicAvailableStatus
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.CastingResource
import heckerpowered.matrix.common.magic.resource.Mana
import heckerpowered.matrix.common.magic.rule.resource.CastingResourcePipeline
import heckerpowered.matrix.common.network.ClientboundSyncHealthPayload
import heckerpowered.matrix.common.persistent.isInfiniteMana
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

/**
 * Represents a single attempt to channel a magic.
 *
 * A ChannelAttempt describes how this specific channeling attempt should
 * be evaluated and executed, including which constraints may be relaxed
 * (e.g. queue lock, mana cost) and how resource payment is handled.
 *
 * An attempt:
 * - may fail without side effects,
 * - interprets certain rejection reasons as acceptable,
 * - and only mutates state (mana / health) when it succeeds.
 *
 * ChannelAttempt does not define magic rules and does not perform channeling
 * itself; it is evaluated by the channeling pipeline to decide whether the
 * attempt may proceed and to apply its cost.
 *
 * Instances are short-lived and must not be reused across multiple attempts.
 *
 * @property bypassLock whether this attempt ignores a locked channel queue
 * @property costMana whether this attempt should consume mana
 */
open class ExecutionPolicy(
    val bypassLock: Boolean = false,
    val costMana: Boolean = true,
    private val ignoredStatuses: Set<MagicAvailableStatus> = emptySet(),
) {
    protected open fun ignoredStatuses(): Set<MagicAvailableStatus> {
        val ignoredStatuses = mutableSetOf<MagicAvailableStatus>()
        ignoredStatuses += this.ignoredStatuses
        if (bypassLock) {
            ignoredStatuses += MagicAvailableStatus.ChannelQueueLocked
        }
        if (!costMana) {
            ignoredStatuses += MagicAvailableStatus.InsufficientMana
        }
        return ignoredStatuses
    }

    open fun isMagicAvailable(availability: MagicAvailability): Boolean {
        val ignoredStatuses = ignoredStatuses()
        return availability.all { it in ignoredStatuses }
    }

    /**
     * Attempts to pay the specified casting cost using all available casting
     * resources resolved from the given invocation.
     *
     * This method performs the **commit phase** of resource consumption:
     * - All applicable [CastingResource] instances are collected from the current
     *   invocation context.
     * - Affordability and consumption rules are evaluated and applied atomically.
     * - On success, the corresponding resources are consumed in priority order.
     *
     * Semantics:
     * - If the invocation does not resolve to a player-backed caster, the payment fails.
     * - If [costMana] is disabled or the caster has infinite mana, the payment succeeds
     *   without consuming any resources.
     * - Convertible resources (e.g. health via Blood Pact) are handled according to
     *   their individual exhaustion rules.
     *
     * This method mutates authoritative server-side state and must only be invoked
     * on the server.
     *
     * @param magic the magic whose casting cost is being paid; used to resolve
     *              magic-specific casting resources and consumption rules.
     * @param cost the effective mana cost required to channel the magic.
     * @param invocation the committed invocation describing the caster, target,
     *                   channel queue, and execution payload.
     * @return `true` if the cost was successfully paid; `false` otherwise.
     */
    open fun payCost(magic: Magic, cost: Mana, invocation: MagicInvocation): Boolean {
        val caster = invocation.caster.asPlayerOrNull() ?: return false
        if (!costMana || caster.isInfiniteMana) {
            return true
        }

        val context = MagicCalculationContext.fromInvocation(invocation)
        val resourceSet = CastingResourcePipeline.collect(context)
        val result = resourceSet.consume(invocation, cost)
        ServerPlayNetworking.send(caster, ClientboundSyncHealthPayload(caster))
        return result
    }
}
