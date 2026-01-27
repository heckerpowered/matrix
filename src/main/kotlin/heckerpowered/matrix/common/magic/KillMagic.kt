/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object KillMagic : Magic(
    MagicDefinition(
        Matrix.identifier("kill"),
        1000.mana,
        (20 * 10).ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: ExecutionPayload) {
        super.cast(player, target, sequence, data)
        target.health = .0f
        val damageSource = MemoryWipeMagic.getDamageSource(player, target, data) { player?.damageSources?.playerAttack(player) }
        target.onDeath(damageSource)
    }
}