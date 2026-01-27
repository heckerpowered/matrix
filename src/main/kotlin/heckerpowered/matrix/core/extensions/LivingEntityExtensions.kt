/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.extensions

import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.item.WizardHelmet13
import heckerpowered.matrix.common.persistent.wizardHelmet
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE
import net.minecraft.entity.player.PlayerEntity

object LivingEntityExtensions {
    val LivingEntity.attackDamage: Double
        get() = getAttributeValue(GENERIC_ATTACK_DAMAGE)

    fun LivingEntity.healOverflow(amount: Float) {
        val actualAmount = amount * healingMultiplier.toFloat()
        if (health + actualAmount <= maxHealth) {
            heal(actualAmount)
            return
        }

        absorptionAmount += (health + actualAmount) - maxHealth
        heal(actualAmount)
    }

    @JvmStatic
    val LivingEntity.healingMultiplier: Double
        get() {
            var multiplier = 1.0
            if (this is PlayerEntity) {
                val item = wizardHelmet.item
                if (item is WizardHelmet13 && isBloodPactActive) {
                    val excess = item.getExcessConversionEfficiency(this, null, null)
                    multiplier += excess * 0.25
                }
            }
            return multiplier
        }
}