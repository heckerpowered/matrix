/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.ModMobEffects
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.defaultMagicDamageSource
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.core.extension.attackDamage
import heckerpowered.matrix.core.extension.damage
import heckerpowered.matrix.core.utility.getNearestEntities
import heckerpowered.matrix.core.utility.withinDistance
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.player.Player

object AbsolvriftMagic : Magic(
    MagicDefinition(
        Matrix.identifier("absolvrift"),
        30.mana,
        36.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.entityOrNull()
        val target = invocation.target
        val damageSource = invocation.defaultMagicDamageSource()
        val amount = caster?.attackDamage?.toFloat() ?: 2.0F

        fun addParticles() {
            if (caster is Player) {
                caster.crit(target)
                caster.magicCrit(target)
            }
        }

        if (target.damage(amount, damageSource)) {
            addParticles()
        }

        val targets = target.getNearestEntities(6.0)
            .withinDistance(target, 6.0)
            .filter { it.isAttackable && it != caster }
        for (it in targets) {
            if (!it.damage(amount, damageSource)) continue
            addParticles()
        }

        caster?.addEffect(MobEffectInstance(ModMobEffects.Absolvrift, 20 * 25, 0, false, false, true))
    }
}