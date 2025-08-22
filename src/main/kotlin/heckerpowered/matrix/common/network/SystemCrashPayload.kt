/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.client.ui.element.SystemCrashBar
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class SystemCrashPayload : CustomPayload {
    companion object {
        val id: CustomPayload.Id<SystemCrashPayload> = CustomPayload.id("system_crash")
        val codec: PacketCodec<PacketByteBuf, SystemCrashPayload> =
            PacketCodec.of(SystemCrashPayload::encode) {
                SystemCrashPayload()
            }
    }

    private fun encode(buffer: PacketByteBuf) {
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return SystemCrashPayload.id
    }

    fun handle(context: Context) {
        context.client().execute {
            SystemCrashBar.systemCrash()
        }
    }
}