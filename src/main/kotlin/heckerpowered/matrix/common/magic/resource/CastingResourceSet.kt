/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.resource

import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.registry.MagicRuleRegistry
import kotlin.math.nextDown

/**
 * Represents a resolved, immutable set of casting resources available
 * under a specific calculation context.
 *
 * A resource set encapsulates:
 * - Priority-based ordering.
 * - Exhaustion semantics.
 * - Aggregate affordability checks.
 * - Deterministic consumption behavior.
 *
 * This type does not:
 * - Discover resources.
 * - Mutate game state by itself.
 * - Perform context-dependent branching.
 *
 * Resource discovery is handled by [MagicRuleRegistry], while
 * consumption is performed explicitly via [consume].
 */
class CastingResourceSet internal constructor(val resources: List<CastingResource>) {
    data class ConsumptionPlan(val resource: CastingResource, val amount: Mana)

    /**
     * Returns whether the given amount of effective mana can be fully
     * satisfied by this resource set.
     *
     * This method performs a pure evaluation and does not mutate any state.
     *
     * Exhaustion rules are respected:
     * - Resources that do not allow exhaustion contribute an open interval.
     *
     * @param context calculation context used for evaluation.
     * @param requiredAmount total effective mana required.
     * @return true if the cost can be satisfied; false otherwise.
     */
    fun canAfford(context: MagicCalculationContext, requiredAmount: Mana): Boolean {
        val required = requiredAmount.toDouble()
        val reserve = resources.sumOf { it.availableAmount(context).toDouble() }
        val allowExhaustion = resources.any { it.allowExhaustion }
        return if (allowExhaustion) reserve >= required else reserve > required
    }

    /**
     * Plans how a required mana amount should be consumed from a set of
     * casting resources under the given calculation context.
     *
     * This function performs a purely evaluative planning step:
     * it does not mutate any resource state and produces a deterministic
     * consumption plan for the same inputs.
     *
     * The planning process follows these rules:
     *
     * 1. Each resource contributes an effective available amount, expressed
     *    in mana units, evaluated under the provided calculation context.
     *
     * 2. If at least one resource does not allow exhaustion, the total available
     *    amount is treated as the supremum of an open interval. In this case,
     *    the required amount must be strictly less than the total available amount.
     *    Otherwise, a closed interval is assumed.
     *
     * 3. Resources are consumed in the order they appear in the input list.
     *    Higher-priority ordering must be applied by the caller beforehand
     *    if required.
     *
     * 4. For each resource:
     *    - If exhaustion is allowed, it may be fully consumed.
     *    - If exhaustion is not allowed, it is consumed as much as possible
     *      without being exhausted.
     *
     * 5. The result is a list of consumption steps, each describing how much
     *    mana-equivalent amount should be consumed from a specific resource.
     *
     * If the required amount cannot be afforded under these rules, an empty
     * list is returned.
     *
     * @param context calculation context used to evaluate resource availability.
     * @param resources ordered list of casting resources participating in payment.
     * @param requiredAmount total mana-equivalent amount required.
     * @return a list of consumption plans, or an empty list if the cost
     *         cannot be afforded.
     */
    fun planConsumption(context: MagicCalculationContext, resources: List<CastingResource>, requiredAmount: Mana): List<ConsumptionPlan> {
        val required = requiredAmount.toDouble()
        val availableAmounts = resources.map { it to it.availableAmount(context).toDouble() }
        val totalAvailable = availableAmounts.sumOf { it.second }
        val requiresOpenBound = resources.any { !it.allowExhaustion }
        val isAffordable = if (requiresOpenBound) totalAvailable > required else totalAvailable >= required
        if (!isAffordable) {
            return emptyList()
        }

        fun consumable(availableAmount: Pair<CastingResource, Double>, amount: Double): Double {
            val resource = availableAmount.first
            val available = availableAmount.second
            val consumable = when {
                amount <= 0.0 -> 0.0
                resource.allowExhaustion -> minOf(available, amount)
                else -> minOf(available, amount).coerceAtMost(available.nextDown())
            }
            return consumable
        }

        return availableAmounts.runningFold(required) { remaining, availableAmount ->
            val consumable = consumable(availableAmount, remaining)
            remaining - consumable
        }.zip(availableAmounts) { remaining, availableAmount ->
            val resource = availableAmount.first
            val consumed = consumable(availableAmount, remaining)
            ConsumptionPlan(resource, Mana(consumed))
        }.filter { it.amount.toDouble() > 0.0 }
    }

    /**
     * Attempts to atomically consume casting resources to pay the required
     * mana-equivalent cost for a magic invocation.
     *
     * This method performs a full resource resolution and consumption in a
     * single, indivisible operation:
     *
     * - All applicable [CastingResource] instances are collected for the
     *   invocation's calculation context.
     * - Resource availability is evaluated according to each resource's
     *   exhaustion rules.
     * - If and only if a valid consumption plan exists, all resources are
     *   consumed in priority order.
     *
     * If no valid solution exists under the current rules (including open-bound
     * exhaustion constraints), this method performs no state mutation and
     * returns `false`.
     *
     * This method is intended to be used exclusively during the commit phase
     * on the server side. It must not be called for prediction or UI purposes.
     *
     * @param invocation the committed magic invocation for which resources
     *                   should be consumed
     * @param requiredAmount the total mana-equivalent amount required
     * @return `true` if the required amount was successfully consumed;
     *         `false` if the calculation has no valid solution
     */
    fun consume(invocation: MagicInvocation, requiredAmount: Mana): Boolean {
        if (requiredAmount.toDouble() <= 0) {
            return true
        }
        val context = MagicCalculationContext.fromInvocation(invocation)
        val plans = planConsumption(context, resources, requiredAmount)
        if (plans.isEmpty()) {
            return false
        }

        for (plan in plans) {
            plan.resource.consume(invocation, plan.amount)
        }
        return true
    }
}