package heckerpowered.matrix.common.network

import heckerpowered.matrix.common.MagicManager
import heckerpowered.matrix.common.persistent.ChannelSequence
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.entity.LivingEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class UseMagicPayload(
    private val index: IntArray,
    private val entityId: Int
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<UseMagicPayload> = CustomPayload.id("use_magic")
        val codec: PacketCodec<PacketByteBuf, UseMagicPayload> =
            PacketCodec.of(UseMagicPayload::encode) { buffer ->
                UseMagicPayload(
                    // Maybe undefined behavior there, I don't know the evaluation order.
                    buffer.readIntArray(buffer.readInt()),
                    buffer.readInt()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(index.size)
        buffer.writeIntArray(index)
        buffer.writeInt(entityId)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return UseMagicPayload.id
    }

    fun handle(context: Context) {
        val player = context.player()

        val targetedEntity = context.player().world.getEntityById(entityId) ?: return
        if (targetedEntity !is LivingEntity) {
            return
        }

        for (i in index) {
            val magic = MagicManager.getMagicById(i) ?: return
            ChannelSequence.channelMagic(magic, player, targetedEntity)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UseMagicPayload

        if (!index.contentEquals(other.index)) return false
        if (entityId != other.entityId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index.contentHashCode()
        result = 31 * result + entityId
        return result
    }
}