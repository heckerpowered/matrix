/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.ModMobEffects
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.channel.removeSourceIfSpoofed
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.level.Level

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

        val duration = if (target.hasEffect(ModMobEffects.Ignite)) 5 else 8
        target.igniteForSeconds(duration.toFloat())
        target.addEffect(MobEffectInstance(ModMobEffects.Ignite, duration * 20, 0, false, true))
        if (target.hasEffect(MobEffects.POISON)) {
            val damageSource = invocation.removeSourceIfSpoofed { caster?.damageSources()?.explosion(target, caster) }
            target.level().explode(caster, damageSource, ExplosionMagic.damageCalculator, target.x, target.y, target.z, 4.0F, false, Level.ExplosionInteraction.MOB)
        }
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailability {
        val availability = super.availableStatus(context)

        val target = context.target
        if (target?.fireImmune() == true) {
            availability += MagicAvailableStatus.TargetImmune
        }

        return availability
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