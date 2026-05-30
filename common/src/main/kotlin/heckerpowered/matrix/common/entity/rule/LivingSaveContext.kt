/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.storage.ValueOutput

data class LivingSaveContext(
    val entity: LivingEntity,
    val output: ValueOutput,
)
