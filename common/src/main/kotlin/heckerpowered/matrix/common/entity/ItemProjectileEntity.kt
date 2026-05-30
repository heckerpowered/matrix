/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ItemProjectileEntity(entityType: EntityType<out ProjectileEntity>, world: World, owner: LivingEntity?, var stack: ItemStack) : ProjectileEntity(entityType, world) {
    init {
        this.owner = owner
    }

    override fun initDataTracker(builder: DataTracker.Builder) {

    }
}