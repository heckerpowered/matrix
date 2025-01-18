package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.util.ActionResult

data class LivingDamageEvent(val entity: LivingEntity, var damageSource: DamageSource, var amount: Float)

fun interface LivingDamageCallback {
    companion object {
        @JvmField
        val event: Event<LivingDamageCallback> =
            EventFactory.createArrayBacked(LivingDamageCallback::class.java) { listeners ->
                LivingDamageCallback { event ->
                    for (listener in listeners) {
                        val result = listener.onHurt(event)
                        if (result != ActionResult.PASS) {
                            return@LivingDamageCallback result
                        }
                    }

                    return@LivingDamageCallback ActionResult.PASS
                }
            }
    }

    fun onHurt(event: LivingDamageEvent): ActionResult
}