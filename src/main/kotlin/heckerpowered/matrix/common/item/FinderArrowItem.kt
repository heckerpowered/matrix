/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.entity.FinderArrowEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ArrowItem
import net.minecraft.item.ItemStack
import net.minecraft.world.World

object FinderArrowItem : ArrowItem(Settings()) {
    override fun createArrow(world: World, stack: ItemStack, shooter: LivingEntity, shotFrom: ItemStack?): PersistentProjectileEntity {
        return FinderArrowEntity(world, shooter, stack.copyWithCount(1), shotFrom)
    }
}