/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.ExplosionMagic.explosionBehavior
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.core.extensions.SequenceExtensions.consumeWhile
import heckerpowered.matrix.core.extensions.SequenceExtensions.drain
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
            val damageSource = MemoryWipeMagic.getDamageSource(player, target, data) { player?.damageSources?.explosion(target, player) }
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
            .filterIsInstance<LivingEntity>()
            .filter { it != target && it != player && it.isAlive }
            .filter { it.getChannelQueue(player)?.isEmpty ?: true }
            .consumeWhile(4) {
                ChannelExecutor.channel(BreakingBadMagic, player, it, ChannelPlan(costMana = false, data = MagicData(isSpread = true))) == MagicAvailableStatus.AVAILABLE
            }
            .drain()
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