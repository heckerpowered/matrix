/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

import net.minecraft.world.entity.LivingEntity

data class LivingHealContext(
    val entity: LivingEntity,
    val referenceHealAmount: Double,
) {
    var healAmount = referenceHealAmount
    var multiplier = 1.0
}