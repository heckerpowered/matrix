/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.asPlayerOrNull
import heckerpowered.matrix.common.magic.channel.defaultMagicDamageSource
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.div
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.resource.Mana.Companion.minus
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.common.persistent.mana
import heckerpowered.matrix.common.persistent.maxMana
import heckerpowered.matrix.core.extension.EntityExtension.damage
import net.minecraft.entity.attribute.EntityAttributes

object DecisiveStrikeMagic : Magic(
    MagicDefinition(
        Matrix.identifier("decisive_strike"),
        16.mana,
        60.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.asPlayerOrNull()
        val target = invocation.target
        val damageSource = invocation.defaultMagicDamageSource()
        target.timeUntilRegen = 0

        // Each 1% missing mana increases damage by 4%
        val missingMana = if (caster == null) {
            .0
        } else {
            ((caster.maxMana - caster.mana) / caster.maxMana)
        }

        val baseDamage = 6.0
        val damageIncreaseBasedOnMaxHealth = (target.maxHealth * 0.14).coerceAtLeast(.0)
        val playerAttackDamage = caster?.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) ?: 2.0

        val damageMultiplierBasedOnCost = (missingMana * 4.0).coerceIn(.0..4.0)
        val damageMultiplierBloodPact = if (caster?.isBloodPactActive == true) {
            1.0
        } else {
            .0
        }

        val damage = baseDamage + damageIncreaseBasedOnMaxHealth + playerAttackDamage
        val amount = damage * (1 + damageMultiplierBasedOnCost + damageMultiplierBloodPact)

        if (target.damage(amount.toFloat(), damageSource)) {
            caster?.addCritParticles(target)
            caster?.addEnchantedHitParticles(target)
        }
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailableStatus {
        val target = context.target
        val damageSource = context.defaultMagicDamageSource()

        if (target?.isInvulnerable == true || target?.isInvulnerableTo(damageSource) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(context)
    }

    override fun getBaseCost(context: MagicCalculationContext): Long {
        val cost = super.getBaseCost(context)
        return when (context.targetRank()) {
            SpellRank.CHIMERA -> cost - 2
            else -> cost
        }
    }
}