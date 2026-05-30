/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.*
import heckerpowered.matrix.common.effect.MatrixStatusEffects.MANA_OVERLOAD_EFFECT
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.util.ActionResult

object ManaOverloadEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    0x98D982
), DamageAttemptRule, DamageComputationRule {
    init {
        CanHaveStatusEffectCallback.EVENT.register(::canHaveStatusEffect)
        RuleRegistry.register<DamageAttemptRule>(this)
        RuleRegistry.register<DamageComputationRule>(this)
    }

    fun isMagicAbilityDisabled(entity: LivingEntity): Boolean {
        return entity.hasStatusEffect(MANA_OVERLOAD_EFFECT)
    }

    private fun canHaveStatusEffect(entity: LivingEntity, effect: StatusEffectInstance): ActionResult {
        val effectAmplifier = entity.getStatusEffect(MANA_OVERLOAD_EFFECT)?.amplifier ?: 0
        if (effectAmplifier >= 1 && effect.effectType.value().isBeneficial) {
            return ActionResult.FAIL
        }

        return ActionResult.PASS
    }

    override fun onAttempt(context: DamageAttemptContext) {
        val attacker = context.attackerAsLiving() ?: return
        val source = context.source
        if (!isMagicAbilityDisabled(attacker)) return
        if (source.isOf(DamageTypes.MAGIC) || source.isOf(DamageTypes.INDIRECT_MAGIC)) {
            context.cancel()
        }
    }

    override fun onComputation(context: DamageComputationContext) {
        val target = context.target
        val effect = target.getStatusEffect(MANA_OVERLOAD_EFFECT) ?: return
        context.damageMultiplier += 0.15
        if (effect.amplifier >= 2) {
            context.baseDamageBonus += target.health * 0.08F
        }
    }
}