package heckerpowered.matrix.common.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.DoubleArgumentType
import heckerpowered.matrix.common.persistent.isInfiniteMana
import heckerpowered.matrix.common.persistent.mana
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

object MatrixCommands {
    fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { commandDispatcher, commandRegistryAccess, registrationEnvironment ->
            commandDispatcher.register(
                literal("matrix").then(
                    literal("mana").requires { it.hasPermissionLevel(2) }.then(
                        literal("set").then(argument("amount", DoubleArgumentType.doubleArg(0.0))).executes {
                            val player = (it.source as ServerCommandSource).playerOrThrow
                            player.mana = DoubleArgumentType.getDouble(it, "amount")
                            return@executes Command.SINGLE_SUCCESS
                        }.then(argument("target", EntityArgumentType.players()).executes {
                            val players = EntityArgumentType.getPlayers(it, "target")
                            players.forEach { player -> player.mana = DoubleArgumentType.getDouble(it, "amount") }
                            return@executes Command.SINGLE_SUCCESS
                        })
                    ).then(
                        literal("infinite").executes {
                            val player = (it.source as ServerCommandSource).playerOrThrow
                            player.isInfiniteMana = !player.isInfiniteMana
                            return@executes Command.SINGLE_SUCCESS
                        }
                    )
                )
            )
        }
    }
}