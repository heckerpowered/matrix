/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.ui.element.SystemCrashBar
import io.netty.buffer.ByteBuf
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data object ClientboundSystemCrashPayload : CustomPacketPayload {
    val payloadId = Matrix.identifier("system_crash")
    val type = CustomPacketPayload.Type<ClientboundSystemCrashPayload>(payloadId)
    val codec = StreamCodec.unit<ByteBuf, ClientboundSystemCrashPayload>(ClientboundSystemCrashPayload)

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(context: Context) {
        context.client().execute {
            SystemCrashBar.systemCrash()
        }
    }
}