/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.item.LightningChestplate1
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeCharge
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeState
import io.netty.buffer.ByteBuf
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.entity.EquipmentSlot

data object ServerboundBorrowedTimePayload : CustomPacketPayload {
    val payloadId = Matrix.identifier("borrowed_time")
    val type = CustomPacketPayload.Type<ServerboundBorrowedTimePayload>(payloadId)
    val codec = StreamCodec.unit<ByteBuf, ServerboundBorrowedTimePayload>(ServerboundBorrowedTimePayload)

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(@Suppress("unused") payload: ServerboundBorrowedTimePayload, context: Context) {
        val player = context.player()
        val lightningChestplate = player.getItemBySlot(EquipmentSlot.CHEST)
        if (lightningChestplate.item !is LightningChestplate1) {
            return
        }

        var currentState = lightningChestplate.components.getOrDefault(borrowedTimeState, false)
        if (currentState) {
            lightningChestplate.set(borrowedTimeState, false)
            player.level().server.playerList.players.forEach {
                ServerPlayNetworking.send(it, ClientboundTeleportPayload(player))
            }
        } else if (lightningChestplate.components.getOrDefault(borrowedTimeCharge, 0) > 0) {
            lightningChestplate.set(borrowedTimeState, true)
        }
        currentState = lightningChestplate.components.getOrDefault(borrowedTimeState, false)
        context.responseSender().sendPacket(ClientboundBorrowedTimePayload(currentState))
    }
}