package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult

val PlayerEntity.bloodPactActive: Boolean
    get() = hasStatusEffect(bloodPactEffect)

object BloodPactEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0xFF0000
) {
    init {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
    }

    private fun onLivingAttack(accumulator: DamageAccumulator): ActionResult {
        val attacker = accumulator.attacker!!
        if (attacker.hasStatusEffect(bloodPactEffect) &&
            accumulator.damageSource.isOf(MatrixDamageTypes.magic)
        ) {
            accumulator.damageMultiplier += 0.1
        }

        return ActionResult.PASS
    }
}