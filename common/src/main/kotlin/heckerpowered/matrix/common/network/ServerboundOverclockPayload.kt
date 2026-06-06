/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.system.PlayerOverclockState
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class ServerboundOverclockPayload(
    private val manaOverclock: Double,
    private val magicOverclock: Double,
) : CustomPacketPayload {
    companion object {
        val payloadId = Matrix.identifier("overclock")
        val type = CustomPacketPayload.Type<ServerboundOverclockPayload>(payloadId)
        val codec = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ServerboundOverclockPayload::manaOverclock,
            ByteBufCodecs.DOUBLE, ServerboundOverclockPayload::magicOverclock,
            ::ServerboundOverclockPayload
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(context: Context) {
        PlayerOverclockState.set(context.player(), manaOverclock, magicOverclock)
    }
}
