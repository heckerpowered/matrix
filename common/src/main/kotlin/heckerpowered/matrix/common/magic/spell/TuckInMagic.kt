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
import heckerpowered.matrix.core.extension.damage
import heckerpowered.matrix.mixin.LivingEntityAccessor
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes

object TuckInMagic : Magic(
    MagicDefinition(
        Matrix.identifier("tuck_in"),
        16.mana,
        60.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.entityOrNull()
        val target = invocation.target
        val damageSource = invocation.defaultMagicDamageSource()

        if (caster == null) {
            val defaultAttackDamage = 2.0
            val damage = (defaultAttackDamage * 5.0).toFloat()
            target.damage(damage, damageSource)
            return
        }

        val attackDamage = caster.getAttributeValue(Attributes.ATTACK_DAMAGE)

        val currentHealth = caster.health.toDouble()
        val currentAbsorptionAmount = caster.absorptionAmount.toDouble()
        val currentEffectiveHealth = currentHealth + currentAbsorptionAmount

        val maxHealth = caster.maxHealth.toDouble()
        val healthRatio = if (maxHealth > 0.0) (currentEffectiveHealth / maxHealth).coerceAtLeast(0.3) else 1.0

        val consumptionAmount = currentEffectiveHealth * 0.075
        consumeFromAbsorptionThenHealth(caster, consumptionAmount)

        val damageAmount = (attackDamage * 5.0 * healthRatio).toFloat()
        target.damage(damageAmount, damageSource)
    }

    private fun consumeFromAbsorptionThenHealth(caster: LivingEntity, consumptionAmount: Double) {
        val currentAbsorptionAmount = caster.absorptionAmount.toDouble()
        val absorptionConsumptionAmount = consumptionAmount.coerceAtMost(currentAbsorptionAmount)
        (caster as LivingEntityAccessor).`matrix$internalSetAbsorptionAmount`((currentAbsorptionAmount - absorptionConsumptionAmount).toFloat())

        val remainingConsumptionAmount = consumptionAmount - absorptionConsumptionAmount
        if (remainingConsumptionAmount <= 0.0) return

        val currentHealth = caster.health.toDouble()
        caster.health = (currentHealth - remainingConsumptionAmount)
            .coerceAtLeast(0.0)
            .toFloat()
    }
}