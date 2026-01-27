/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.network.SystemCrashPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object SystemCrashMagic : Magic(
    MagicDefinition(
        Matrix.identifier("system_crash"),
        100.mana,
        200.ticks
    )
) {

    override fun channel(player: PlayerEntity, target: LivingEntity, queue: ChannelQueue, data: ExecutionPayload) {
        if (target is ServerPlayerEntity) {
            ServerPlayNetworking.send(target, SystemCrashPayload())
        }
    }
}