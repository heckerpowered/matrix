/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

data class EquipItemContext(val entity: LivingEntity, val slot: EquipmentSlot, val oldStack: ItemStack, val stack: ItemStack)