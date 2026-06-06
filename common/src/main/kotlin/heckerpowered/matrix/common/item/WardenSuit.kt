/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.effect.ModMobEffects
import net.minecraft.world.entity.LivingEntity

fun LivingEntity.isWardenArmorAngered(): Boolean {
    return getEffect(ModMobEffects.Angered) != null
}