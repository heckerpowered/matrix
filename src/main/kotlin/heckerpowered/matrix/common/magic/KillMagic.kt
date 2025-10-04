/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.persistent.ChannelQueue
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object KillMagic : Magic(
    MagicDefinition(
        Matrix.identifier("kill"),
        1000.mana,
        (20 * 10).ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: MagicData) {
        super.cast(player, target, sequence, data)
        target.health = .0f
        val damageSource = MemoryEraseMagic.getDamageSource(player, target, sequence) { player?.damageSources?.playerAttack(player) }
        target.onDeath(damageSource)
    }
}