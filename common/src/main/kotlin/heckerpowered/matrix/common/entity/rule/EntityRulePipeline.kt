/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.all

object EntityRulePipeline {
    @JvmStatic
    fun onComputation(context: AttributeComputationContext) {
        for (rule in RuleRegistry.all<AttributeComputationRule>()) {
            rule.onComputation(context)
        }
    }

    @JvmStatic
    fun onUpdate(context: EntityUpdateContext) {
        for (rule in RuleRegistry.all<EntityUpdateRule>()) {
            rule.onUpdate(context)
        }
    }

    @JvmStatic
    fun onEquipItem(context: EquipItemContext) {
        for (rule in RuleRegistry.all<EquipItemRule>()) {
            rule.onEquipItem(context)
        }
    }

    @JvmStatic
    fun onKnockback(context: KnockbackContext) {
        for (rule in RuleRegistry.all<KnockbackRule>()) {
            rule.onKnockback(context)
            if (context.isCancelled) return
        }
    }

    @JvmStatic
    fun onLivingDeath(context: LivingDeathContext) {
        for (rule in RuleRegistry.all<LivingDeathRule>()) {
            rule.onLivingDeath(context)
            if (!context.allow) return
        }
    }

    @JvmStatic
    fun onHeal(context: LivingHealContext) {
        for (rule in RuleRegistry.all<LivingHealRule>()) {
            rule.onHeal(context)
        }
    }

    @JvmStatic
    fun canBeAffected(context: EffectRestrictionContext) {
        for (rule in RuleRegistry.all<EffectRestrictionRule>()) {
            rule.canBeAffected(context)
            if (!context.isAllowed) return
        }
    }

    @JvmStatic
    fun onEffectRemoval(context: EffectRemovalContext) {
        for (rule in RuleRegistry.all<EffectRemovalRule>()) {
            rule.onRemoval(context)
            if (!context.isAllowed) return
        }
    }

    @JvmStatic
    fun onEffectRemoved(context: EffectRemovedContext) {
        for (rule in RuleRegistry.all<EffectRemovedRule>()) {
            rule.onRemoved(context)
        }
    }
}