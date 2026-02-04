/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.resource

import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.MagicCalculationContext

/**
 * Represents a resource that can contribute to paying the casting cost of a magic.
 *
 * A casting resource does not define *how* costs are calculated globally.
 * Instead, it declares:
 * - how much effective mana it can provide under a given calculation context
 * - whether it is allowed to be fully exhausted
 * - in which order it should be consumed relative to other resources
 */
interface CastingResource {

    /**
     * Determines the consumption priority of this resource.
     *
     * Resources with higher priority values are consumed first.
     *
     * Example:
     * - Mana: priority = 100
     * - Health (via Blood Pact): priority = 50
     */
    val priority: Int

    /**
     * Indicates whether this resource is allowed to be fully exhausted.
     *
     * If this returns `false`, the available amount provided by this resource
     * represents an open interval: the coordinator may consume arbitrarily
     * close to the returned amount, but must not consume it entirely.
     */
    val allowExhaustion: Boolean

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
    fun availableAmount(context: MagicCalculationContext): Mana

    /**
     * Consumes the specified effective mana amount from this resource.
     *
     * This method is only invoked during the commit phase on the server side,
     * after affordability has been validated.
     *
     * The provided amount is guaranteed to respect this resource's exhaustion
     * constraints as defined by [allowExhaustion].
     */
    fun consume(invocation: MagicInvocation, amount: Mana)
}