/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.command

import com.mojang.brigadier.Command
import heckerpowered.matrix.common.persistent.isInfiniteMana
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands.hasPermission
import net.minecraft.commands.Commands.literal
import net.minecraft.server.permissions.PermissionCheck
import net.minecraft.server.permissions.Permissions

object MatrixCommands {

    fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal("matrix").then(
                    literal("mana")
                        .requires(hasPermission(PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
                        .then(literal("infinite"))
                        .executes { context ->
                            val player = context.source.playerOrException
                            player.isInfiniteMana = !player.isInfiniteMana
                            return@executes Command.SINGLE_SUCCESS
                        }
                )
            )
        }
    }
}