/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.client.render.ScreenEffectRenderer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class WitherArmorTriggerPayload : CustomPayload {
    companion object {
        val id: CustomPayload.Id<WitherArmorTriggerPayload> = CustomPayload.id("wither_armor_trigger")
        val codec: PacketCodec<PacketByteBuf, WitherArmorTriggerPayload> =
            PacketCodec.of(WitherArmorTriggerPayload::encode) {
                WitherArmorTriggerPayload()
            }
    }

    private fun encode(buffer: PacketByteBuf) {
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return WitherArmorTriggerPayload.id
    }

    fun handle(context: Context) {
        ScreenEffectRenderer.onWitherArmorEffectApplied()
    }
}