/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.entity.FinderArrowEntity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.item.ArrowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

object FinderArrowItem : ArrowItem(Properties()) {
    override fun createArrow(level: Level, itemStack: ItemStack, owner: LivingEntity, firedFromWeapon: ItemStack?): AbstractArrow {
        return FinderArrowEntity(level, owner, itemStack.copyWithCount(1), firedFromWeapon)
    }
}