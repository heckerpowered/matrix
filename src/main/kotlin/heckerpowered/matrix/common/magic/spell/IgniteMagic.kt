/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.IGNITE_EFFECT
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.channel.removeSourceIfSpoofed
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.world.World

object IgniteMagic : Magic(
    MagicDefinition(
        Matrix.identifier("ignite"),
        9.mana,
        40.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.entityOrNull()
        val target = invocation.target
        invocation.payload

        val duration = if (target.hasStatusEffect(IGNITE_EFFECT)) 5 else 8
        target.setOnFireFor(duration.toFloat())
        target.addStatusEffect(StatusEffectInstance(IGNITE_EFFECT, duration * 20, 0, false, true))
        if (target.hasStatusEffect(StatusEffects.POISON)) {
            val damageSource = invocation.removeSourceIfSpoofed { caster?.damageSources?.explosion(target, caster) }
            target.world.createExplosion(caster, damageSource, ExplosionMagic.explosionBehavior, target.x, target.y, target.z, 4.0F, false, World.ExplosionSourceType.MOB)
        }
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailableStatus {
        val target = context.target
        if (target?.isFireImmune == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(context)
    }

    override fun getBaseCost(context: MagicCalculationContext): Long {
        if (context.targetRank() == SpellRank.CHIMERA) {
            return super.getBaseCost(context) - 1
        }
        return super.getBaseCost(context)
    }

    override fun getBaseChannelTime(context: MagicCalculationContext): Long {
        val channelTime = super.getBaseChannelTime(context)
        return when (context.targetRank()) {
            SpellRank.CHIMERA -> channelTime + 2 * 20
            SpellRank.BOSS -> channelTime + 20
            else -> channelTime
        }
    }
}