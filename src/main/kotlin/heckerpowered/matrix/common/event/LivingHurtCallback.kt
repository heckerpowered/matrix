package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.util.ActionResult

data class LivingHurtEvent(val entity: LivingEntity, var damageSource: DamageSource, var amount: Float)

fun interface LivingHurtCallback {
    companion object {
        @JvmField
        val event: Event<LivingHurtCallback> =
            EventFactory.createArrayBacked(LivingHurtCallback::class.java) { listeners ->
                LivingHurtCallback { event ->
                    for (listener in listeners) {
                        val result = listener.onHurt(event)
                        if (result != ActionResult.PASS) {
                            return@LivingHurtCallback result
                        }
                    }

                    return@LivingHurtCallback ActionResult.PASS
                }
            }
    }

    fun onHurt(event: DamageAccumulator): ActionResult
}