/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.persistent.ChannelQueue
import heckerpowered.matrix.common.persistent.magicClock
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World
import net.minecraft.world.explosion.AdvancedExplosionBehavior
import java.util.*

object ExplosionMagic : Magic(
    MagicDefinition(
        Matrix.identifier("explosion"),
        30.mana,
        30.ticks
    )
) {
    val explosionBehavior = AdvancedExplosionBehavior(
        false,
        true,
        Optional.empty(),
        Optional.empty()
    )

    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: MagicData) {
        super.cast(player, target, sequence, data)
        val damageSource = MemoryEraseMagic.getDamageSource(player, target, sequence) { target.world.damageSources.create(MatrixDamageTypes.magic, player) }

        target.world.createExplosion(player, damageSource, explosionBehavior, target.x, target.y, target.z, ((player?.magicClock ?: 1.0) * 4.0).toFloat(), false, World.ExplosionSourceType.MOB)
        // if (target.world is ServerWorld) {
        //     target.world.server?.playerManager?.playerList?.forEach {
        //         ServerPlayNetworking.send(it, ExplosionPayload(target.id))
        //     }
        // }
    }
}