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
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import net.minecraft.world.World
import net.minecraft.world.explosion.AdvancedExplosionBehavior
import java.util.*

object ExplosionMagic : Magic(
    MagicDefinition(
        Matrix.identifier("explosion"),
        30.mana,
        30.ticks
    )
) {
    val explosionBehavior = AdvancedExplosionBehavior(
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

        target.world.createExplosion(caster, damageSource, explosionBehavior, target.x, target.y, target.z, 4.0F, false, World.ExplosionSourceType.MOB)
    }
}