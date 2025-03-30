package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity

object WizardHelmetApogee : WizardHelmet(
    11.0,
    Settings()
        .fireproof()
        .rarity(Rarity.RARE)
        .component(MatrixComponents.maxLoad, 20.0)
) {
    init {
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

        if (attacker.wizardHelmet.item !is WizardHelmetApogee) {
            return ActionResult.PASS
        }
        event.damageMultiplier += 0.85
        if ((0..100).random() <= 35) {
            event.damageMultiplier += 1.0
            attacker.addCritParticles(event.target)
            attacker.addEnchantedHitParticles(event.target)
        }
        return ActionResult.PASS
    }
}