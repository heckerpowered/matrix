/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.core

import heckerpowered.matrix.common.magic.channel.CasterContext
import heckerpowered.matrix.common.magic.channel.ChannelQueue
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.resources.ResourceKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

class MagicCalculationContext(
    val caster: CasterContext? = null,
    val target: LivingEntity? = null,
    val queue: ChannelQueue? = null,
    val payload: ExecutionPayload = ExecutionPayload(),
) {
    companion object {
        fun fromInvocation(invocation: MagicInvocation): MagicCalculationContext {
            return MagicCalculationContext(
                caster = invocation.caster,
                target = invocation.target,
                queue = invocation.queue,
                payload = invocation.payload
            )
        }

        fun fromEntity(caster: LivingEntity?, target: LivingEntity?, payload: ExecutionPayload = ExecutionPayload()): MagicCalculationContext {
            val casterContext = caster?.let { CasterContext.fromEntity(it) }
            val queue = when {
                caster != null && target != null ->
                    (caster as? Player)?.let { target.getChannelQueue(it) }

                else -> null
            }

            return MagicCalculationContext(
                caster = casterContext,
                target = target,
                queue = queue,
                payload = payload,
            )
        }
    }

    fun entityOrNull(): LivingEntity? = caster?.entityOrNull()
    fun playerOrNull(): Player? = caster?.entityOrNull() as? Player
}

fun MagicCalculationContext.wipedMagicDamageSource(): DamageSource {
    val level = caster?.level ?: target?.level() ?: error("MagicCalculationContext requires a level")
    return level.damageSources().source(MatrixDamageTypes.magic)
}

fun MagicCalculationContext.defaultMagicDamageSource(): DamageSource {
    return defaultDamageSource(MatrixDamageTypes.magic)
}

fun MagicCalculationContext.defaultDamageSource(type: ResourceKey<DamageType>): DamageSource {
    val level = caster?.level ?: target?.level() ?: error("MagicCalculationContext requires a level")
    if (payload.isSpoofed) {
        return level.damageSources().source(type)
    }

    val attacker = caster?.entityOrNull()
    return if (attacker != null) {
        level.damageSources().source(type, attacker)
    } else {
        level.damageSources().source(type)
    }
}

fun MagicCalculationContext.removeSourceIfSpoofed(sourceSupplier: () -> DamageSource?): DamageSource {
    val level = caster?.level ?: target?.level() ?: error("MagicCalculationContext requires a level")
    val wipedDamageSource = level.damageSources().source(MatrixDamageTypes.magic)
    if (payload.isSpoofed) {
        return wipedDamageSource
    }

    caster?.entityOrNull() ?: return wipedDamageSource
    return sourceSupplier() ?: wipedDamageSource
}

fun MagicCalculationContext.targetProfile(): SpellProfile? {
    return target?.let { SpellProfile.getProfile(target) }
}

fun MagicCalculationContext.targetRank(fallback: SpellRank = SpellRank.NORMAL): SpellRank {
    return targetProfile()?.rank ?: fallback
}

fun MagicCalculationContext.targetForm(fallback: SpellForm = SpellForm.NATURAL): SpellForm {
    return targetProfile()?.form ?: fallback
}
