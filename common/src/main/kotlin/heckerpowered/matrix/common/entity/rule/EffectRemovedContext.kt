/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity

data class EffectRemovedContext(
    val entity: LivingEntity,
    val effectInstance: MobEffectInstance,
) {
    val effect: MobEffect
        get() = effectInstance.effect.value()
}