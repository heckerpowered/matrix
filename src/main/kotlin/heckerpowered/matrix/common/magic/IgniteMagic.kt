/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.IGNITE_EFFECT
import heckerpowered.matrix.common.magic.ExplosionMagic.explosionBehavior
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.persistent.ChannelQueue
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World

object IgniteMagic : Magic(
    MagicDefinition(
        Matrix.identifier("ignite"),
        9.mana,
        40.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: MagicData) {
        super.cast(player, target, sequence, data)
        val duration = if (target.hasStatusEffect(IGNITE_EFFECT)) {
            5F
        } else {
            8F
        }
        target.setOnFireFor(duration * 20)
        target.addStatusEffect(StatusEffectInstance(IGNITE_EFFECT, 5 * 20, 0, false, true))
        if (target.hasStatusEffect(StatusEffects.POISON)) {
            val damageSource = MemoryEraseMagic.getDamageSource(player, target, sequence) { player?.damageSources?.explosion(target, player) }
            target.world.createExplosion(player, damageSource, explosionBehavior, target.x, target.y, target.z, 4.0F, false, World.ExplosionSourceType.MOB)
            // if (target.world is ServerWorld) {
            //     target.world.server?.playerManager?.playerList?.forEach {
            //         ServerPlayNetworking.send(it, ExplosionPayload(target.id))
            //     }
            // }
        }
    }

    override fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?): MagicAvailableStatus {
        if (target?.isFireImmune == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}