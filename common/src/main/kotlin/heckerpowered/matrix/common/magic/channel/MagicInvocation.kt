/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.channel

import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getOrCreateChannelQueue
import heckerpowered.matrix.common.magic.core.*
import net.minecraft.resources.ResourceKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

/**
 * Describes a single magic invocation at a specific point in the casting lifecycle.
 *
 * A [MagicInvocation] is a *fact object*: it captures all contextual information
 * required to evaluate magic rules and apply effects, without performing any logic
 * by itself.
 *
 * Core responsibilities:
 * - Binds a caster identity ([CasterContext]) to a concrete target.
 * - Associates the invocation with its channeling state ([ChannelQueue]).
 * - Carries execution-scoped data via [ExecutionPolicy].
 *
 * Design notes:
 * - The caster is represented as a [CasterContext] rather than a player or entity
 *   to decouple magic logic from entity lifetime and online state.
 * - The caster may be detached when the invocation is evaluated; callers are expected
 *   to refresh the identity (e.g. via `caster.tryResolve()`) at the appropriate phase
 *   such as cast time.
 * - [MagicInvocation] is intentionally immutable. Phase transitions (e.g. channel →
 *   cast) should be represented by creating a new instance rather than mutating state.
 *
 * Typical usage:
 * - Created by the channel executor when a magic begins channeling.
 * - Passed through magic hooks (channel / cast) as a stable context object.
 * - Interpreted by magic implementations to apply rules and effects.
 *
 * This type exists to keep magic APIs stable over time: additional contextual
 * information should be added here rather than expanding magic method signatures.
 *
 * @property caster the rule-level identity of the magic caster at this moment
 * @property target the living entity being affected by the magic
 * @property queue the channel queue this invocation belongs to
 * @property payload execution-scoped data propagated across invocations
 */
data class MagicInvocation(
    val caster: CasterContext,
    val target: LivingEntity,
    val queue: ChannelQueue,
    val payload: ExecutionPolicy = ExecutionPolicy(),
) {
    companion object {
        /**
         * Creates a magic invocation from a living entity caster and a target.
         *
         * This factory resolves the caster into a [CasterContext] and ensures
         * a channel queue exists for the given target.
         *
         * This is the canonical entry point for server-side magic invocation
         * initiated by entities.
         */
        fun fromEntity(caster: Player, target: LivingEntity, payload: ExecutionPolicy = ExecutionPolicy()): MagicInvocation {
            val casterContext = CasterContext.fromEntity(caster)
            val queue = target.getOrCreateChannelQueue(caster)

            return MagicInvocation(
                caster = casterContext,
                target = target,
                queue = queue,
                payload = payload
            )
        }
    }
}

fun MagicInvocation.defaultMagicDamageSource(): DamageSource {
    return MagicCalculationContext.fromInvocation(this).defaultMagicDamageSource()
}

fun MagicInvocation.defaultDamageSource(type: ResourceKey<DamageType>): DamageSource {
    return MagicCalculationContext.fromInvocation(this).defaultDamageSource(type)
}

fun MagicInvocation.removeSourceIfSpoofed(sourceSupplier: () -> DamageSource?): DamageSource {
    return MagicCalculationContext.fromInvocation(this).removeSourceIfSpoofed(sourceSupplier)
}

fun MagicInvocation.wipedMagicDamageSource(): DamageSource {
    return MagicCalculationContext.fromInvocation(this).wipedMagicDamageSource()
}