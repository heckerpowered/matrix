/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.*
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.core.extension.SequenceExtension.consumeWhile
import heckerpowered.matrix.core.extension.SequenceExtension.drain
import heckerpowered.matrix.core.utility.getAdjacentEntities
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

object BreakingBadMagic : Magic(
    MagicDefinition(
        Matrix.identifier("breaking_bad"),
        12.mana,
        40.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val target = invocation.target
        val caster = invocation.caster.entityOrNull()
        val payload = invocation.payload

        target.addEffect(MobEffectInstance(MobEffects.POISON, 20 * 5, 4))
        target.addEffect(MobEffectInstance(MobEffects.BLINDNESS, 20 * 5, 4))

        if (target.isOnFire) {
            val damageSource = invocation.removeSourceIfSpoofed { caster?.damageSources()?.explosion(target, caster) }
            target.level().explode(
                caster,
                damageSource,
                ExplosionMagic.damageCalculator,
                target.x,
                target.y,
                target.z,
                4.0F,
                false,
                Level.ExplosionInteraction.MOB
            )
        }

        if (caster !is Player || payload.isSpread) {
            return
        }

        target.getAdjacentEntities(8.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != target && it != caster && it.isAlive }
            .filter { it.getChannelQueue(caster)?.isEmpty ?: true }
            .consumeWhile(4) {
                val spreadPayload = ExecutionPayload(isSpread = true)
                val spreadInvocation = MagicInvocation.fromEntity(caster, it, spreadPayload)
                val spreadAttempt = ExecutionPayload(costMana = false)
                val result = ChannelExecutor.channel(BreakingBadMagic, spreadInvocation, spreadAttempt)
                result == LMagicAvailableStatus.AVAILABLE
            }
            .drain()
    }

    override fun availableStatus(context: MagicCalculationContext): LMagicAvailableStatus {
        val target = context.target
        if (target?.isInvulnerableToEffect(MobEffects.POISON) == true ||
            target?.isInvulnerableToEffect(MobEffects.BLINDNESS) == true
        ) {
            return LMagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(context)
    }
}