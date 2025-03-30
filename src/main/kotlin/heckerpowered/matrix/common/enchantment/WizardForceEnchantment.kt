package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult

object WizardForceEnchantment {
    fun onInitialize() {
        LivingAttackCallback.event.register(::onLivingAttack)
    }

    private fun onLivingAttack(event: DamageAccumulator): ActionResult {
        val attacker = event.attacker!!
        if (attacker !is ServerPlayerEntity) {
            return ActionResult.PASS
        }
        if (!event.damageSource.isOf(MatrixDamageTypes.magic)) {
            return ActionResult.PASS
        }

        val level = attacker.wizardHelmet.getEnchantmentLevel(wizardForce)
        event.damageMultiplier += level * 0.2
        return ActionResult.PASS
    }
}