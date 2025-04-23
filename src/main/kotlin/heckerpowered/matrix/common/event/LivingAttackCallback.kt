package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.util.ActionResult

fun interface LivingAttackCallback {
    companion object {
        @JvmField
        val EVENT: Event<LivingAttackCallback> =
            EventFactory.createArrayBacked(LivingAttackCallback::class.java) { listeners ->
                LivingAttackCallback { event ->
                    for (listener in listeners) {
                        val result = listener.onAttack(event)
                        if (result != ActionResult.PASS) {
                            return@LivingAttackCallback result
                        }
                    }

                    return@LivingAttackCallback ActionResult.PASS
                }
            }
    }

    fun onAttack(event: DamageAccumulator): ActionResult
}