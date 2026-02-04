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
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity

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
            val queue = if (caster != null && target != null) {
                (caster as? PlayerEntity)?.let { target.getChannelQueue(it) }
            } else {
                null
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
    fun playerOrNull(): PlayerEntity? = caster?.entityOrNull() as? PlayerEntity
}

fun MagicCalculationContext.defaultMagicDamageSource(): DamageSource {
    val world = caster?.world ?: target?.world ?: error("MagicCalculationContext requires a world")
    if (payload.isSpoofed) {
        return world.damageSources.create(MatrixDamageTypes.magic)
    }

    val attacker = caster?.entityOrNull()
    return if (attacker != null) {
        world.damageSources.create(MatrixDamageTypes.magic, attacker)
    } else {
        world.damageSources.create(MatrixDamageTypes.magic)
    }
}

fun MagicCalculationContext.removeSourceIfSpoofed(sourceSupplier: () -> DamageSource?): DamageSource {
    val world = caster?.world ?: target?.world ?: error("MagicCalculationContext requires a world")
    val wipedDamageSource = world.damageSources.create(MatrixDamageTypes.magic)
    if (payload.isSpoofed) {
        return wipedDamageSource
    }

    caster?.entityOrNull() ?: return wipedDamageSource
    return sourceSupplier() ?: wipedDamageSource
}