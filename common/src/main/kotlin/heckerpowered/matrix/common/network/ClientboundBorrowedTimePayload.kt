/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.item.LightningChestplate1
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeState
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.entity.EquipmentSlot

class ClientboundBorrowedTimePayload(private val state: Boolean) : CustomPacketPayload {
    companion object {
        val payloadId = Matrix.identifier("borrowed_time")
        val type = CustomPacketPayload.Type<ClientboundBorrowedTimePayload>(payloadId)
        val codec = StreamCodec.composite(ByteBufCodecs.BOOL, ClientboundBorrowedTimePayload::state, ::ClientboundBorrowedTimePayload)
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(context: Context) {
        val player = context.player()
        val lightningChestplate = player.getItemBySlot(EquipmentSlot.CHEST)
        if (lightningChestplate.item !is LightningChestplate1) return

        lightningChestplate.set(borrowedTimeState, state)
    }
}