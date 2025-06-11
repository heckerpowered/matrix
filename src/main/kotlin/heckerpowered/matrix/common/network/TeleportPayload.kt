package heckerpowered.matrix.common.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.entity.Entity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.Vec3d

class TeleportPayload(
    val entityId: Int,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
) : CustomPayload {
    constructor(entity: Entity) : this(entity.id, entity.x, entity.y, entity.z, entity.yaw, entity.pitch)

    companion object {
        val id: CustomPayload.Id<TeleportPayload> = CustomPayload.id("teleport")
        val codec: PacketCodec<PacketByteBuf, TeleportPayload> =
            PacketCodec.of(TeleportPayload::encode) { buffer ->
                val entityId = buffer.readInt()
                val x = buffer.readDouble()
                val y = buffer.readDouble()
                val z = buffer.readDouble()
                val yaw = buffer.readFloat()
                val pitch = buffer.readFloat()
                TeleportPayload(entityId, x, y, z, yaw, pitch)
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(entityId)
        buffer.writeDouble(x)
        buffer.writeDouble(y)
        buffer.writeDouble(z)
        buffer.writeFloat(yaw)
        buffer.writeFloat(pitch)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return TeleportPayload.id
    }

    fun handle(context: Context) {
        val world = context.player().world
        val entity = world.getEntityById(entityId) ?: return
        entity.setPos(x, y, z)
        entity.trackedPosition.pos = Vec3d(x, y, z)
        entity.updateTrackedPositionAndAngles(x, y, z, yaw, pitch, 0)
        entity.refreshPositionAndAngles(x, y, z, yaw, pitch)
    }
}