package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound

fun interface ReadDataCallback {
    companion object {
        @JvmField
        val event: Event<ReadDataCallback> = EventFactory.createArrayBacked(ReadDataCallback::class.java) { listeners ->
            ReadDataCallback { entity, nbt ->
                for (listener in listeners) {
                    listener.readData(entity, nbt)
                }
            }
        }
    }

    fun readData(entity: LivingEntity, nbt: NbtCompound)
}