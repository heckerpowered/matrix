/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.defaultMagicDamageSource
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.core.SpellRank.BOSS
import heckerpowered.matrix.common.magic.core.SpellRank.CHIMERA
import heckerpowered.matrix.common.magic.core.targetRank
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.common.persistent.magicClock
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.SimpleExplosionDamageCalculator
import java.util.*

object ExplosionMagic : Magic(
    MagicDefinition(
        Matrix.identifier("explosion"),
        24.mana,
        20.ticks
    )
) {
    val damageCalculator = SimpleExplosionDamageCalculator(
        false,
        true,
        Optional.empty(),
        Optional.empty()
    )

    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.entityOrNull()
        val target = invocation.target
        val damageSource = invocation.defaultMagicDamageSource()

        // Pre-migration jar: explosion power is the caster's magic overclock rate (HUD N key,
        // 1.0 when absent or not a player) times the 4.0 base.
        val power = ((caster as? ServerPlayer)?.magicClock ?: 1.0) * 4.0
        target.level().explode(caster, damageSource, damageCalculator, target.x, target.y, target.z, power.toFloat(), false, Level.ExplosionInteraction.MOB)
    }

    override fun getBaseChannelTime(context: MagicCalculationContext): Long {
        val channelTime = super.getBaseChannelTime(context)
        return when (context.targetRank()) {
            BOSS -> channelTime + 6 * 20
            CHIMERA -> 100
            else -> channelTime
        }
    }
}