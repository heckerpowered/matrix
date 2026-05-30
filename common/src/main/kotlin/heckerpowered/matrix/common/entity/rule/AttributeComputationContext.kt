/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

import net.minecraft.core.Holder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute

data class AttributeComputationContext(
    val entity: LivingEntity,
    val attribute: Holder<Attribute>,
    val referenceValue: Double,
) {
    var baseValue = referenceValue
    var multiplier = 1.0

    val finalValue: Double
        get() = baseValue * multiplier
}
