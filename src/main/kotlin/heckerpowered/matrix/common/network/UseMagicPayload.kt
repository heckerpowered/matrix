package heckerpowered.matrix.common.network

import heckerpowered.matrix.common.MagicManager
import heckerpowered.matrix.common.persistent.ChannelSequence
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.entity.LivingEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class UseMagicPayload(
    private val index: Int,
    private val entityId: Int
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<UseMagicPayload> = CustomPayload.id("use_magic")
        val codec: PacketCodec<PacketByteBuf, UseMagicPayload> =
            PacketCodec.of(UseMagicPayload::encode) { buffer ->
                UseMagicPayload(
                    // Maybe undefined behavior there, I don't know the evaluation order.
                    buffer.readInt(),
                    buffer.readInt()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(index)
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

        val magic = MagicManager.getMagicById(index) ?: return
        ChannelSequence.channelMagic(magic, player, targetedEntity)
    }
}