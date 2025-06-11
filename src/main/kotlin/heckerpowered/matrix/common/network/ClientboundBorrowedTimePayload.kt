package heckerpowered.matrix.common.network

import heckerpowered.matrix.common.item.LightningChestplate1
import heckerpowered.matrix.common.item.MatrixComponents.BORROWED_TIME_STATE
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.entity.EquipmentSlot
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class ClientboundBorrowedTimePayload(private val state: Boolean) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<ClientboundBorrowedTimePayload> = CustomPayload.id("clientbound_borrowed_time")
        val codec: PacketCodec<PacketByteBuf, ClientboundBorrowedTimePayload> =
            PacketCodec.of(ClientboundBorrowedTimePayload::encode) { buffer ->
                ClientboundBorrowedTimePayload(buffer.readBoolean())
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeBoolean(state)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ClientboundBorrowedTimePayload.id
    }

    fun handle(context: Context) {
        val player = context.player()
        val lightningChestplate = player.getEquippedStack(EquipmentSlot.CHEST)
        if (lightningChestplate.item !is LightningChestplate1) {
            return
        }

        lightningChestplate.set(BORROWED_TIME_STATE, state)
    }
}