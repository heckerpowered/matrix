/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.player
import heckerpowered.matrix.common.magic.channel.*
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.core.extensions.SequenceExtensions.consumeWhile
import heckerpowered.matrix.core.extensions.SequenceExtensions.drain
import heckerpowered.matrix.core.utility.EntitySearch.getAdjacentEntities
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World

object BreakingBadMagic : Magic(
    MagicDefinition(
        Matrix.identifier("breaking_bad"),
        9.mana,
        40.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val target = invocation.target
        val caster = invocation.caster.entityOrNull()
        val payload = invocation.payload

        target.addStatusEffect(StatusEffectInstance(StatusEffects.POISON, 20 * 5, 4))
        target.addStatusEffect(StatusEffectInstance(StatusEffects.BLINDNESS, 20 * 5, 4))

        if (target.isOnFire) {
            val damageSource = invocation.removeSourceIfSpoofed { caster?.damageSources?.explosion(target, caster) }
            target.world.createExplosion(caster, damageSource, ExplosionMagic.explosionBehavior, target.x, target.y, target.z, 4.0F, false, World.ExplosionSourceType.MOB)
        }

        if (caster !is PlayerEntity || payload.isSpread) {
            return
        }

        target.getAdjacentEntities(8.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != target && it != caster && it.isAlive }
            .filter { it.getChannelQueue(caster)?.isEmpty ?: true }
            .consumeWhile(4) {
                val spreadInvocation = MagicInvocation.fromEntity(player, it)
                val spreadPayload = ExecutionPayload(isSpread = true)
                val spreadAttempt = ChannelAttempt(costMana = false, payload = spreadPayload)
                ChannelExecutor.channel(BreakingBadMagic, spreadInvocation, spreadAttempt) == MagicAvailableStatus.AVAILABLE
            }
            .drain()
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailableStatus {
        val target = context.target
        if (target?.isInvulnerableToEffect(StatusEffects.POISON) == true ||
            target?.isInvulnerableToEffect(StatusEffects.BLINDNESS) == true
        ) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(context)
    }
}