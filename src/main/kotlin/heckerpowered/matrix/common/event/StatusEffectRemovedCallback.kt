package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.util.ActionResult

fun interface StatusEffectRemovedCallback {
    companion object {
        @JvmField
        val EVENT: Event<StatusEffectRemovedCallback> =
            EventFactory.createArrayBacked(StatusEffectRemovedCallback::class.java) { listeners ->
                StatusEffectRemovedCallback { entity, statusEffectInstance ->
                    for (listener in listeners) {
                        val result = listener.onStatusEffectRemoved(entity, statusEffectInstance)
                        if (result != ActionResult.PASS) {
                            return@StatusEffectRemovedCallback result
                        }
                    }

                    return@StatusEffectRemovedCallback ActionResult.PASS
                }
            }
    }

    fun onStatusEffectRemoved(entity: LivingEntity, statusEffectInstance: StatusEffectInstance): ActionResult
}