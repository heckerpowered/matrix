/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import heckerpowered.matrix.common.entity.ModEntityTypes.FINDER_ARROW_ENTITY
import heckerpowered.matrix.common.item.FinderArrowItem
import net.minecraft.core.BlockPos
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

class FinderArrowEntity : AbstractArrow {
    constructor(level: Level, owner: LivingEntity, stack: ItemStack, shotFrom: ItemStack?) : super(FINDER_ARROW_ENTITY, owner, level, stack, shotFrom)
    constructor(level: Level) : super(FINDER_ARROW_ENTITY, level)
    constructor(entityType: EntityType<out FinderArrowEntity>, level: Level) : super(entityType, level)

    override fun tick() {
        super.tick()
        if (isInGround) {
            return
        }
        val previousPosition = Vec3(xo, yo, zo)
        val currentPosition = position()
        val searchBox = AABB(previousPosition, currentPosition).inflate(12.0, 9999.0, 12.0)
        level().getEntities(getOwner(), searchBox) { true }
            .filterIsInstance<LivingEntity>()
            .filter {
                val blockPos = BlockPos.containing(it.position())
                distanceToSqr(it) <= 144 // 144 = 12 * 12 (radius)
                        || level().canSeeSky(blockPos)
            }
            .forEach {
                val statusEffectInstance = MobEffectInstance(MobEffects.GLOWING, 20 * 5, 0)
                it.addEffect(statusEffectInstance, this)
            }
    }

    override fun getDefaultPickupItem(): ItemStack {
        return ItemStack(FinderArrowItem)
    }
}
