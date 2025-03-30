package heckerpowered.matrix.common.event

import heckerpowered.matrix.core.Accumulator
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.entry.RegistryEntry

fun interface GetAttributeValueCallback {
    companion object {
        @JvmField
        val event: Event<GetAttributeValueCallback> =
            EventFactory.createArrayBacked(GetAttributeValueCallback::class.java) { listeners ->
                GetAttributeValueCallback { entity, attribute, accumulator ->
                    for (listener in listeners) {
                        listener.getAttributeValue(entity, attribute, accumulator)
                    }
                }
            }
    }

    fun getAttributeValue(entity: LivingEntity, attribute: RegistryEntry<EntityAttribute>, accumulator: Accumulator)
}