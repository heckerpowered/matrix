/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.ABSOLVRIFT_EFFECT
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.defaultMagicDamageSource
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.core.extensions.EntityExtensions.damage
import heckerpowered.matrix.core.extensions.LivingEntityExtensions.attackDamage
import heckerpowered.matrix.core.utility.EntitySearch.getNearestEntities
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity

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
            if (caster is PlayerEntity) {
                caster.addCritParticles(target)
                caster.addEnchantedHitParticles(target)
            }
        }

        if (target.damage(amount, damageSource)) {
            addParticles()
        }

        target.getNearestEntities(6.0)
            .filter { it.isAttackable && it != caster }
            .forEach {
                if (it.damage(amount, damageSource)) {
                    addParticles()
                }
            }

        caster?.addStatusEffect(StatusEffectInstance(ABSOLVRIFT_EFFECT, 20 * 25, 0, false, false, true))
    }
}