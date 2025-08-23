/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.common.item.LightningChestplate1
import heckerpowered.matrix.common.item.MatrixComponents.BORROWED_TIME_CHARGE
import heckerpowered.matrix.common.item.MatrixComponents.BORROWED_TIME_STATE
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
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
        if (lightningChestplate.item !is LightningChestplate1) {
            return
        }

        var currentState = lightningChestplate.components.getOrDefault(BORROWED_TIME_STATE, false)
        if (currentState) {
            lightningChestplate.set(BORROWED_TIME_STATE, false)
            player.server.playerManager.playerList.forEach {
                ServerPlayNetworking.send(it, TeleportPayload(player))
            }
        } else if (lightningChestplate.components.getOrDefault(BORROWED_TIME_CHARGE, 0) > 0) {
            lightningChestplate.set(BORROWED_TIME_STATE, true)
        }
        currentState = lightningChestplate.components.getOrDefault(BORROWED_TIME_STATE, false)
        context.responseSender().sendPacket(ClientboundBorrowedTimePayload(currentState))
    }
}