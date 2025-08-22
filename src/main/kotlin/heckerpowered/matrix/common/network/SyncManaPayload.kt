/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.client.MatrixHud
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class SyncManaPayload(
    private val mana: Double,
    private val maxMana: Double,
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<SyncManaPayload> = CustomPayload.id("sync_mana")
        val codec: PacketCodec<PacketByteBuf, SyncManaPayload> =
            PacketCodec.of(SyncManaPayload::encode) { buffer ->
                SyncManaPayload(
                    buffer.readDouble(),
                    buffer.readDouble()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeDouble(mana)
        buffer.writeDouble(maxMana)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return SyncManaPayload.id
    }

    fun handle(context: Context) {
        if (mana.isNaN() || maxMana.isNaN()) {
            return
        }
        context.client().execute {
            MatrixHud.maxMana = maxMana
            if (mana.isInfinite()) {
                MatrixHud.mana = mana
                return@execute
            }

            val currentMana = MatrixHud.mana - MatrixHud.manaUsage
            if (currentMana > mana) {
                MatrixHud.manaUsage += currentMana - mana
            } else {
                MatrixHud.mana += mana - currentMana
            }
            MatrixHud.onRemoteManaUpdate()
        }
    }
}