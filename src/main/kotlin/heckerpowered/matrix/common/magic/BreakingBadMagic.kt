/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.ExplosionMagic.explosionBehavior
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.persistent.ChannelQueue
import heckerpowered.matrix.common.persistent.getChannelSequence
import heckerpowered.matrix.core.extensions.SequenceExtensions.consumeWhile
import heckerpowered.matrix.core.utility.EntitySearch.getAdjacentEntities
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World

object BreakingBadMagic : Magic(
    MagicDefinition(
        Matrix.identifier("breaking_bad"),
        9.mana,
        40.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: MagicData) {
        super.cast(player, target, sequence, data)
        target.addStatusEffect(StatusEffectInstance(StatusEffects.POISON, 20 * 5, 4))
        target.addStatusEffect(StatusEffectInstance(StatusEffects.BLINDNESS, 20 * 5, 4))

        if (target.isOnFire) {
            val damageSource = MemoryEraseMagic.getDamageSource(player, target, sequence) { player?.damageSources?.explosion(target, player) }
            target.world.createExplosion(player, damageSource, explosionBehavior, target.x, target.y, target.z, 4.0F, false, World.ExplosionSourceType.MOB)
            // if (target.world is ServerWorld) {
            //     target.world.server?.playerManager?.playerList?.forEach {
            //         ServerPlayNetworking.send(it, ExplosionPayload(target.id))
            //     }
            // }
        }

        if (player == null || data.isSpread) {
            return
        }

        target.getAdjacentEntities(8.0)
            .filter {
                it is LivingEntity
                        && (it.getChannelSequence(player)?.channelingMagicCount() ?: 0) == 0
                        && it != player
                        && it.isAlive
            }
            .map { it as LivingEntity }
            .consumeWhile(4) {
                ChannelQueue.channelMagic(BreakingBadMagic, player, it, false, data = MagicData(true))
            }
        // var spreadTarget = target
        // repeat(4) {
        //     val nearestEntity = spreadTarget.getNearestEntities(8.0) {
        //         it is LivingEntity
        //                 && (it.getChannelSequence(player)?.channelingMagicCount() ?: 0) == 0
        //                 && it != player
        //                 && it.isAlive
        //     }
        //     if (nearestEntity == null || nearestEntity !is LivingEntity) {
        //         return
        //     }
//
        //     ChannelSequence.channelMagic(BreakingBadMagic, player, nearestEntity, false, data = MagicData(true))
//
        //     spreadTarget = nearestEntity
        // }
    }

    override fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?): MagicAvailableStatus {
        if (target?.isInvulnerableToEffect(StatusEffects.POISON) == true ||
            target?.isInvulnerableToEffect(StatusEffects.BLINDNESS) == true
        ) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}