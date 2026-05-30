/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.all
import heckerpowered.matrix.common.rule.forEach

object DamagePipeline {
    @JvmStatic
    fun attempt(context: DamageAttemptContext) {
        for (rule in RuleRegistry.all<DamageAttemptRule>()) {
            rule.onAttempt(context)
            if (context.isCancelled) return
        }
    }

    @JvmStatic
    fun computation(context: DamageComputationContext) {
        for (rule in RuleRegistry.all<DamageComputationRule>()) {
            rule.onComputation(context)
            if (context.isCancelled) return
        }
    }

    @JvmStatic
    fun realization(context: DamageRealizationContext) {
        RuleRegistry.forEach<DamageRealizationRule> { it.onRealization(context) }
    }

    @JvmStatic
    fun outcome(context: DamageOutcomeContext) {
        RuleRegistry.forEach<DamageOutcomeRule> { it.onOutcome(context) }
    }

    @JvmStatic
    fun settlement(context: DamageSettlementContext) {
        RuleRegistry.forEach<DamageSettlementRule> { it.onSettlement(context) }
    }
}

