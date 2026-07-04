/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.ledger.transaction.constraint.BoundedTransactionConstraint
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.ManaLedger
import heckerpowered.matrix.common.magic.system.ManaLedger.toLedgerUnits
import heckerpowered.matrix.common.persistent.OverclockState
import heckerpowered.matrix.core.mana
import heckerpowered.matrix.core.maxMana
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

/**
 * Sent by the client HUD (see [heckerpowered.matrix.client.MatrixHud]) whenever the player
 * adjusts their mana/magic overclock ratio (N/M keys).
 *
 * [handle] was restored from the pre-migration jar's `OverclockPayload`: rates are clamped
 * to `1.0..10.0` and persisted per player in [OverclockState]. The old handler's follow-up
 * effects are owned by the ledger system now — max mana scaling is contributed by
 * [heckerpowered.matrix.common.magic.system.ManaOverclockRule] (mana above the shrunk bound
 * is clamped by the account's transaction constraints) and the HUD sync happens through the
 * regeneration ticker's per-tick `ClientboundSyncManaPayload`.
 */
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
        val player = context.player()
        val data = OverclockState.getPlayerState(player)
        data.manaOverclock = manaOverclock.coerceIn(1.0, 10.0)
        data.magicOverclock = magicOverclock.coerceIn(1.0, 10.0)

        // The pre-migration handler applied the new max mana immediately: it wrote the scaled
        // value and clamped mana down to it. The bound lives in the ledger account's
        // transaction constraints now and is normally only refreshed on equip, so refresh it
        // here with the overclocked max mana and extinguish any mana above the shrunk bound.
        val maxMana = player.maxMana
        ManaLedger.account(player).transactionConstraints =
            setOf(BoundedTransactionConstraint(0, maxMana.toLedgerUnits()))
        val excess = player.mana.toDouble() - maxMana.toDouble()
        if (excess > 0) {
            ManaLedger.extinguishMana(player, excess.mana)
        }
    }
}
