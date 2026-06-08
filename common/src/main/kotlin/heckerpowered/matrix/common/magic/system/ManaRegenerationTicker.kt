/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.system

import heckerpowered.ledger.account.coerceTransferUnits
import heckerpowered.ledger.account.incomingRemainingUnits
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.rule.calculation.pipeline.CalculationPipeline
import heckerpowered.matrix.common.magic.rule.calculation.sink.ManaRegenerationCalculationSink
import heckerpowered.matrix.common.magic.system.ManaLedger.toLedgerUnits
import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.network.ClientboundSyncManaPayload
import heckerpowered.matrix.core.isInfiniteMana
import heckerpowered.matrix.core.mana
import heckerpowered.matrix.core.maxMana
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object ManaRegenerationTicker {
    fun onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(::onServerTick)
    }

    private fun onServerTick(minecraftServer: MinecraftServer) {
        if (minecraftServer.tickCount % 20 != 0) return

        for (player in minecraftServer.playerList.players) {
            WizardHelmet.syncManaBounds(player)
            regenerateMana(player)
            syncMana(player)
        }
    }

    private fun regenerateMana(player: ServerPlayer) {
        val account = ManaLedger.account(player)
        if (player.isInfiniteMana) {
            val maximum = account.incomingRemainingUnits() ?: return
            if (maximum <= 0L) return
            ManaLedger.Authority.postTransfer(account, maximum)
            return
        }

        val context = MagicCalculationContext.fromEntity(player, null)
        val sink = ManaRegenerationCalculationSink()
        CalculationPipeline.apply(context, sink)

        val regenerationAmount = (sink.regeneration * sink.multiplier).mana.toLedgerUnits()
        val transferAmount = regenerationAmount.coerceTransferUnits(ManaLedger.Authority, account)
        if (transferAmount <= 0L) return

        ManaLedger.Authority.postTransfer(account, transferAmount)
    }

    private fun syncMana(player: ServerPlayer) {
        ServerPlayNetworking.send(
            player,
            ClientboundSyncManaPayload(
                player.mana.toDouble(),
                player.maxMana.toDouble(),
                player.isInfiniteMana,
            ),
        )
    }
}
