/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.extension

import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.entity.ability.HealMeasurement
import heckerpowered.matrix.common.item.WizardHelmet13
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player

val LivingEntity.attackDamage: Double
    get() = getAttributeValue(Attributes.ATTACK_DAMAGE)

fun LivingEntity.healOverflow(amount: Float) {
    val actualAmount = amount * healingMultiplier.toFloat()
    if (health + actualAmount <= maxHealth) {
        heal(actualAmount)
        return
    }

    absorptionAmount += (health + actualAmount) - maxHealth
    heal(actualAmount)
}

fun LivingEntity.healMeasured(amount: Float): HealMeasurement {
    
}

val LivingEntity.healingMultiplier: Double
    get() {
        var multiplier = 1.0
        if (this is Player) {
            val item = wizardHelmetStack.item
            if (item is WizardHelmet13 && isBloodPactActive) {
                val calculationContext = MagicCalculationContext.fromEntity(this, null)
                val excess = item.getExcessExchangeRate(calculationContext)
                multiplier += excess * 0.25
            }
        }
        return multiplier
    }

