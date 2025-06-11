package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity

/**
 * Wizard Helmet 5 'Axiom of Annihilation'
 */
object WizardHelmet5 : WizardHelmet(
    12.0,
    Settings()
        .fireproof()
        .rarity(Rarity.EPIC)
        .component(MatrixComponents.MAX_LOAD, 20.0)
) {
    init {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
    }

    private fun onLivingAttack(event: DamageAccumulator): ActionResult {
        val attacker = event.attacker!!
        if (attacker !is ServerPlayerEntity) {
            return ActionResult.PASS
        }
        if (!event.damageSource.isOf(MatrixDamageTypes.magic)) {
            return ActionResult.PASS
        }

        if (attacker.wizardHelmet.item is WizardHelmet5) {
            event.damageMultiplier += 1
        }
        return ActionResult.PASS
    }
}