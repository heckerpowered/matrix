/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class ItemProjectileEntity(entityType: EntityType<out Projectile>, world: Level, owner: LivingEntity?, var stack: ItemStack) : Projectile(entityType, world) {
    init {
        this.setOwner(owner)
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {

    }
}