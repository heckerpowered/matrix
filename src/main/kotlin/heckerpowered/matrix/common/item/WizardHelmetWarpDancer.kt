package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity

object WizardHelmetWarpDancer : WizardHelmet(
    12.0,
    Settings()
        .fireproof()
        .rarity(Rarity.EPIC)
        .component(MatrixComponents.maxLoad, 20.0)
) {
    private fun onLivingAttack(event: DamageAccumulator): ActionResult {
        val attacker = event.attacker!!
        if (attacker !is ServerPlayerEntity) {
            return ActionResult.PASS
        }
        if (!event.damageSource.isOf(MatrixDamageTypes.magic)) {
            return ActionResult.PASS
        }

        if (attacker.wizardHelmet.item is WizardHelmetWarpDancer) {
            event.damageMultiplier += 1
        }
        return ActionResult.PASS
    }
}