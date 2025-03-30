package heckerpowered.matrix.common.network

import heckerpowered.matrix.common.item.LightningChestplateBorrowedTime
import heckerpowered.matrix.common.item.borrowedTimeChargeComponent
import heckerpowered.matrix.common.item.borrowedTimeStateComponent
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.entity.EquipmentSlot
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class BorrowedTimePayload : CustomPayload {
    companion object {
        val id: CustomPayload.Id<BorrowedTimePayload> = CustomPayload.id("borrowed_time")
        val codec: PacketCodec<PacketByteBuf, BorrowedTimePayload> =
            PacketCodec.of(BorrowedTimePayload::encode) {
                BorrowedTimePayload()
            }
    }

    private fun encode(buffer: PacketByteBuf) {
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return BorrowedTimePayload.id
    }

    fun handle(context: Context) {
        val player = context.player()
        val lightningChestplate = player.getEquippedStack(EquipmentSlot.CHEST)
        if (lightningChestplate.item !is LightningChestplateBorrowedTime) {
            return
        }

        var currentState = lightningChestplate.components.getOrDefault(borrowedTimeStateComponent, false)
        if (currentState) {
            lightningChestplate.set(borrowedTimeStateComponent, false)
        } else if (lightningChestplate.components.getOrDefault(borrowedTimeChargeComponent, 0) > 0) {
            lightningChestplate.set(borrowedTimeStateComponent, true)
        }
        currentState = lightningChestplate.components.getOrDefault(borrowedTimeStateComponent, false)
        context.responseSender().sendPacket(ClientboundBorrowedTimePayload(currentState))
    }
}