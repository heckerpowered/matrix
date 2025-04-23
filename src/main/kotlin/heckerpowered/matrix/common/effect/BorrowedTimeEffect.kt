package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.item.borrowedTimeActive
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult

object BorrowedTimeEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0x5A89C0
) {
    init {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
    }

    private fun onLivingAttack(event: DamageAccumulator): ActionResult {
        if (event.attacker is PlayerEntity && event.attacker.borrowedTimeActive) {
            event.target.timeUntilRegen = 0
        }
        return ActionResult.PASS
    }
}