/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.render.ScreenEffectRenderer
import io.netty.buffer.ByteBuf
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data object ClientboundWitherArmorTriggerPayload : CustomPacketPayload {
    val payloadId = Matrix.identifier("wither_armor_trigger")
    val type = CustomPacketPayload.Type<ClientboundWitherArmorTriggerPayload>(payloadId)
    val codec = StreamCodec.unit<ByteBuf, ClientboundWitherArmorTriggerPayload>(ClientboundWitherArmorTriggerPayload)

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(context: Context) {
        ScreenEffectRenderer.onWitherArmorEffectApplied()
    }
}