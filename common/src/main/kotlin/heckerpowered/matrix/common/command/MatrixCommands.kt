/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.DoubleArgumentType
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.ManaLedger
import heckerpowered.matrix.common.network.ClientboundSyncManaPayload
import heckerpowered.matrix.common.persistent.isInfiniteMana
import heckerpowered.matrix.core.mana
import heckerpowered.matrix.core.maxMana
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.hasPermission
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionCheck
import net.minecraft.server.permissions.Permissions

/**
 * `/matrix mana ...` debug commands, tree restored from the pre-migration jar:
 * `set <amount> [<targets>]` writes an absolute mana value (clamped to `0..maxMana`) and
 * `infinite` toggles infinite mana for the executing player.
 */
object MatrixCommands {

    fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal("matrix").then(
                    literal("mana")
                        .requires(hasPermission(PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
                        .then(
                            literal("set").then(
                                argument("amount", DoubleArgumentType.doubleArg(0.0))
                                    .executes { context ->
                                        setMana(context.source.playerOrException, DoubleArgumentType.getDouble(context, "amount"))
                                        Command.SINGLE_SUCCESS
                                    }
                                    .then(
                                        argument("target", EntityArgument.players()).executes { context ->
                                            val amount = DoubleArgumentType.getDouble(context, "amount")
                                            EntityArgument.getPlayers(context, "target").forEach { setMana(it, amount) }
                                            Command.SINGLE_SUCCESS
                                        }
                                    )
                            )
                        )
                        .then(
                            literal("infinite").executes { context ->
                                val player = context.source.playerOrException
                                player.isInfiniteMana = !player.isInfiniteMana
                                Command.SINGLE_SUCCESS
                            }
                        )
                )
            )
        }
    }

    /**
     * The pre-migration `setMana` wrote the clamped value into the persistent mana pool and
     * synced immediately; mana is a ledger balance now, so reach the same clamped target by
     * issuing/extinguishing the difference.
     */
    private fun setMana(player: ServerPlayer, amount: Double) {
        val target = amount.coerceIn(0.0, player.maxMana.toDouble().coerceAtLeast(0.0))
        val delta = target - player.mana.toDouble()
        when {
            delta > 0 -> ManaLedger.issueMana(player, delta.mana)
            delta < 0 -> ManaLedger.extinguishMana(player, (-delta).mana)
        }
        ServerPlayNetworking.send(player, ClientboundSyncManaPayload(player.mana.toDouble(), player.maxMana.toDouble()))
    }
}
