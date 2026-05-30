/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

import net.minecraft.core.Holder
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.LivingEntity

data class EffectRemovalContext(
    val entity: LivingEntity,
    val effect: Holder<MobEffect>,
) {
    var isAllowed: Boolean = true
        private set

    fun reject() {
        isAllowed = false
    }
}
