/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

class ClientboundSyncManaPayload(
    private val mana: Double,
    private val maxMana: Double,
) : CustomPacketPayload {
    companion object {
        val payloadId = Matrix.identifier("sync_mana")
        val type = CustomPacketPayload.Type<ClientboundSyncManaPayload>(payloadId)
        val codec = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ClientboundSyncManaPayload::mana,
            ByteBufCodecs.DOUBLE, ClientboundSyncManaPayload::maxMana,
            ::ClientboundSyncManaPayload
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(context: Context) {
        if (mana.isNaN() || maxMana.isNaN()) {
            return
        }
        context.client().execute {
            val previousMana = MatrixHud.mana
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

            val player = context.player()
            (player.wizardHelmetStack.item as? WizardHelmet)?.onManaChanged(player, previousMana, mana)
        }
    }
}