package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingHurtCallback
import heckerpowered.matrix.common.persistent.allChannelSequences
import net.minecraft.util.ActionResult

object QueueMasteryEnchantment {
    fun onInitialize() {
        LivingHurtCallback.event.register(::onLivingHurt)
    }

    private fun onLivingHurt(event: DamageAccumulator): ActionResult {
        // Queue Mastery: +15% damage against enemies with a locked queue.
        val lockedCount = event.target.allChannelSequences.values.count { it.locked }
        event.damageMultiplier += 0.15 * lockedCount
        return ActionResult.PASS
    }
}