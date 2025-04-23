package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound

fun interface WriteDataCallback {
    companion object {
        @JvmField
        val EVENT: Event<WriteDataCallback> =
            EventFactory.createArrayBacked(WriteDataCallback::class.java) { listeners ->
                WriteDataCallback { entity, nbt ->
                    for (listener in listeners) {
                        listener.writeData(entity, nbt)
                    }
                }
            }
    }

    fun writeData(entity: LivingEntity, nbt: NbtCompound)
}