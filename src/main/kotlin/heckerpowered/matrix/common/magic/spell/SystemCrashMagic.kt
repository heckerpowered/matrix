/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.common.network.SystemCrashPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity

object SystemCrashMagic : Magic(
    MagicDefinition(
        Matrix.identifier("system_crash"),
        100.mana,
        200.ticks
    )
) {

    override fun channel(invocation: MagicInvocation) {
        val target = invocation.target as? ServerPlayerEntity ?: return
        ServerPlayNetworking.send(target, SystemCrashPayload())
    }
}