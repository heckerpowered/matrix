/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.extension

import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.entity.ability.HealMeasurement
import heckerpowered.matrix.common.entity.ability.HealMeasurementScope
import heckerpowered.matrix.common.item.WizardHelmet13
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player

val LivingEntity.attackDamage: Double
    get() = getAttribute(Attributes.ATTACK_DAMAGE)?.value ?: 0.0

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
    return HealMeasurementScope.measure {
        heal(amount)
    }
}

/**
 * Adds absorption amount up to [maximumAmount].
 *
 * This function never lowers the current absorption amount. If the current
 * absorption amount is already greater than [maximumAmount], it is left unchanged.
 *
 * @return the actually added absorption amount.
 */
fun LivingEntity.addAbsorptionUpTo(amount: Float, maximumAmount: Float): Float {
    if (amount <= 0.0F) return 0.0F

    val currentAmount = absorptionAmount
    if (currentAmount >= maximumAmount) return 0.0F

    val addedAmount = amount.coerceAtMost(maximumAmount - currentAmount)
    internalSetAbsorptionAmount(currentAmount + addedAmount)
    return addedAmount
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

