/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.*
import heckerpowered.matrix.common.entity.rule.EffectRestrictionContext
import heckerpowered.matrix.common.entity.rule.EffectRestrictionRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity

object ManaOverloadEffect : MobEffect(
    MobEffectCategory.HARMFUL,
    0x98D982
), EffectRestrictionRule, DamageAttemptRule, DamageComputationRule {
    init {
        RuleRegistry.register<EffectRestrictionRule>(this)
        RuleRegistry.register<DamageAttemptRule>(this)
        RuleRegistry.register<DamageComputationRule>(this)
    }

    fun isMagicAbilityDisabled(entity: LivingEntity): Boolean {
        return entity.hasEffect(ModMobEffects.ManaOverload)
    }

    override fun canBeAffected(context: EffectRestrictionContext) {
        val entity = context.entity
        val effectInstance = context.effectInstance

        val effectAmplifier = entity.getEffect(ModMobEffects.ManaOverload)?.amplifier ?: 0
        if (effectAmplifier >= 1 && effectInstance.effect.value().isBeneficial) {
            context.reject()
        }
    }

    override fun onAttempt(context: DamageAttemptContext) {
        val attacker = context.attackerAsLiving() ?: return
        val source = context.source
        if (!isMagicAbilityDisabled(attacker)) return
        if (source.`is`(DamageTypes.MAGIC) || source.`is`(DamageTypes.INDIRECT_MAGIC)) {
            context.cancel()
        }
    }

    override fun onComputation(context: DamageComputationContext) {
        val target = context.target
        val effect = target.getEffect(ModMobEffects.ManaOverload) ?: return
        context.damageMultiplier += 0.15
        if (effect.amplifier >= 2) {
            context.baseDamageBonus += target.health * 0.08F
        }
    }
}