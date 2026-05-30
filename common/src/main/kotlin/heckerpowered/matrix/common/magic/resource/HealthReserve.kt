/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.resource

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.BloodPactEffect
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.asPlayerOrNull
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana

class HealthReserve(override val priority: Int = 50) : CastingResource {
    /**
     * Indicates whether this resource is allowed to be fully exhausted.
     *
     * If this returns `false`, the available amount provided by this resource
     * represents an open interval: the coordinator may consume arbitrarily
     * close to the returned amount, but must not consume it entirely.
     */
    override val allowExhaustion: Boolean = false

    /**
     * Returns the maximum effective mana this resource can provide
     * under the given calculation context.
     *
     * This method must be:
     * - pure (no side effects)
     * - deterministic for the same context
     *
     * If [allowExhaustion] is false, the returned value represents
     * the supremum of an open interval rather than a closed bound.
     */
    override fun availableAmount(context: MagicCalculationContext): Mana {
        val player = context.playerOrNull() ?: return 0.mana
        val convertRatio = BloodPactEffect.getExchangeRate(context)
        return (player.health * convertRatio).mana
    }

    /**
     * Consumes the specified effective mana amount from this resource.
     *
     * This method is only invoked during the commit phase on the server side,
     * after affordability has been validated.
     *
     * The provided amount is guaranteed to respect this resource's exhaustion
     * constraints as defined by [allowExhaustion].
     */
    override fun consume(invocation: MagicInvocation, amount: Mana) {
        val player = invocation.caster.asPlayerOrNull() ?: return
        val context = MagicCalculationContext.fromInvocation(invocation)
        val convertRatio = BloodPactEffect.getExchangeRate(context)
        val healthConsumed = (amount.toDouble() / convertRatio).toFloat()

        val playerHealth = player.health
        player.health -= healthConsumed
        if (!allowExhaustion && playerHealth > 0 && playerHealth - healthConsumed < 0) {
            Matrix.LOGGER.error("HealthReserve was exhausted during consume, player.health=$playerHealth, healthConsumed=$healthConsumed")
        }
    }
}