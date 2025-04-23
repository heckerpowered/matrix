package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.event.*
import heckerpowered.matrix.common.tag.MatrixDamageTypes
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
        EntityTickCallback.EVENT.register(::onEntityTick)
    }

    private fun onEntityTick(entity: LivingEntity) {
        val effect = entity.getStatusEffect(manaOverloadEffect) ?: return
        if (effect.amplifier >= 2 && entity.age % 20 == 0) {
            entity.damage(entity.damageSources.create(MatrixDamageTypes.magic), entity.health * 0.08F)
        }
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
        return entity.hasStatusEffect(manaOverloadEffect)
    }

    private fun canHaveStatusEffect(entity: LivingEntity, effect: StatusEffectInstance): ActionResult {
        val effectAmplifier = entity.getStatusEffect(manaOverloadEffect)?.amplifier ?: 0
        if (effectAmplifier >= 1 && effect.effectType.value().isBeneficial) {
            return ActionResult.FAIL
        }

        return ActionResult.PASS
    }

    private fun onLivingHurt(accumulator: DamageAccumulator): ActionResult {
        val target = accumulator.target
        if (target.hasStatusEffect(manaOverloadEffect)) {
            accumulator.damageMultiplier += 0.15
        }

        return ActionResult.PASS
    }
}