package heckerpowered.matrix.common.network

import heckerpowered.matrix.client.MatrixHud
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class SyncManaPayload(
    private val mana: Double,
    private val maxMana: Double
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<SyncManaPayload> = CustomPayload.id("use_magic")
        val codec: PacketCodec<PacketByteBuf, SyncManaPayload> =
            PacketCodec.of(SyncManaPayload::encode) { buffer ->
                SyncManaPayload(
                    buffer.readDouble(),
                    buffer.readDouble()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeDouble(mana)
        buffer.writeDouble(maxMana)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return SyncManaPayload.id
    }

    fun handle(context: Context) {
        context.client().execute {
            MatrixHud.mana = mana
            MatrixHud.maxMana = maxMana
        }
    }
}