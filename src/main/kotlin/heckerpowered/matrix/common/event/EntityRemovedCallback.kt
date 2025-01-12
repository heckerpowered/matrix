package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.Entity

fun interface EntityRemovedCallback {
    companion object {
        @JvmField
        val EVENT: Event<EntityRemovedCallback> = EventFactory.createArrayBacked(EntityRemovedCallback::class.java) { listeners ->
            EntityRemovedCallback { entity, removalReason ->
                for (listener in listeners) {
                    listener.onEntityRemoved(entity, removalReason)
                }
            }
        }
    }

    fun onEntityRemoved(entity: Entity, removalReason: Entity.RemovalReason)
}