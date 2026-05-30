/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity

data class EffectRestrictionContext(
    val entity: LivingEntity,
    val effect: MobEffectInstance,
) {
    var isAllowed: Boolean = true
        private set

    fun reject() {
        isAllowed = false
    }
}
