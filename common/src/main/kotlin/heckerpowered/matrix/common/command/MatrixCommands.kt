/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg
import com.mojang.brigadier.arguments.DoubleArgumentType.getDouble
import heckerpowered.matrix.common.magic.resource.Mana
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.ManaLedger
import heckerpowered.matrix.common.network.ClientboundSyncManaPayload
import heckerpowered.matrix.common.persistent.isInfiniteMana
import heckerpowered.matrix.common.persistent.maxMana
import heckerpowered.matrix.common.persistent.mana
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.hasPermission
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionCheck
import net.minecraft.server.permissions.Permissions

object MatrixCommands {

    fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal("matrix").then(
                    literal("mana")
                        .requires(hasPermission(PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
                        .then(
                            literal("set")
                                .then(
                                    argument("amount", doubleArg(0.0))
                                        .executes { context ->
                                            val player = context.source.playerOrException
                                            setMana(player, getDouble(context, "amount").mana)
                                            return@executes Command.SINGLE_SUCCESS
                                        }
                                        .then(
                                            argument("target", EntityArgument.players())
                                                .executes { context ->
                                                    EntityArgument.getPlayers(context, "target").forEach { player ->
                                                        setMana(player, getDouble(context, "amount").mana)
                                                    }
                                                    return@executes Command.SINGLE_SUCCESS
                                                }
                                        )
                                )
                        )
                        .then(
                            literal("infinite")
                                .executes { context ->
                                    val player = context.source.playerOrException
                                    player.isInfiniteMana = !player.isInfiniteMana
                                    if (player.isInfiniteMana) {
                                        ManaLedger.setMana(player, player.maxMana)
                                    }
                                    syncMana(player)
                                    return@executes Command.SINGLE_SUCCESS
                                }
                        )
                )
            )
        }
    }

    private fun setMana(player: ServerPlayer, amount: Mana) {
        ManaLedger.setMana(player, amount)
        syncMana(player)
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
