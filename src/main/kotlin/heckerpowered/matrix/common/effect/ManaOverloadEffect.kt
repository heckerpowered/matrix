/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.effect.MatrixStatusEffects.MANA_OVERLOAD_EFFECT
import heckerpowered.matrix.common.event.CanHaveStatusEffectCallback
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.event.LivingHurtCallback
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.util.ActionResult

object ManaOverloadEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    0x98D982
) {
    init {
        LivingHurtCallback.EVENT.register(::onLivingHurt)
        CanHaveStatusEffectCallback.EVENT.register(::canHaveStatusEffect)
        LivingAttackCallback.EVENT.register(::onLivingAttack)
    }

    private fun onLivingAttack(accumulator: DamageAccumulator): ActionResult {
        if (isMagicAbilityDisabled(accumulator.attacker!!) &&
            (accumulator.damageSource.isOf(DamageTypes.MAGIC) || accumulator.damageSource.isOf(DamageTypes.INDIRECT_MAGIC))
        ) {
            return ActionResult.FAIL
        }

        return ActionResult.PASS
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

    private fun onLivingHurt(accumulator: DamageAccumulator): ActionResult {
        val target = accumulator.target
        val effect = target.getStatusEffect(MANA_OVERLOAD_EFFECT) ?: return ActionResult.PASS
        accumulator.damageMultiplier += 0.15
        if (effect.amplifier >= 2) {
            accumulator.baseDamageBonus += target.health * 0.08F
        }

        return ActionResult.PASS
    }
}