/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import heckerpowered.matrix.common.entity.ModEntityTypes.FINDER_ARROW_ENTITY
import heckerpowered.matrix.common.item.FinderArrowItem
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.item.ItemStack
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class FinderArrowEntity : AbstractArrow {
    constructor(world: Level, owner: LivingEntity, stack: ItemStack, shotFrom: ItemStack?) : super(FINDER_ARROW_ENTITY, owner, world, stack, shotFrom)
    constructor(world: Level) : super(FINDER_ARROW_ENTITY, world)

    override fun tick() {
        super.tick()
        if (level().isClientSide && !isInGround) {
            // 26.2: INSTANT_EFFECT particles carry a color option; white with full power
            // matches the pre-migration default appearance.
            level().addParticle(
                net.minecraft.core.particles.SpellParticleOption.create(ParticleTypes.INSTANT_EFFECT, 1.0F, 1.0F, 1.0F, 1.0F),
                x, y, z, 0.0, 0.0, 0.0
            )
        }

        if (isInGround) {
            return
        }
        val previousPosition = Vec3(xo, yo, zo)
        val currentPosition = position()
        val searchBox = AABB(previousPosition, currentPosition).inflate(12.0, 9999.0, 12.0)
        level().getEntities(getOwner(), searchBox)
            .filterIsInstance<LivingEntity>()
            .filter {
                val blockPos = BlockPos.containing(it.x, it.y, it.z)
                this.distanceToSqr(it) <= 144 // 144 = 12 * 12 (radius)
                        || it.level().canSeeSky(blockPos)
            }
            .forEach {
                val statusEffectInstance = MobEffectInstance(MobEffects.GLOWING, 20 * 5, 0)
                it.addEffect(statusEffectInstance, effectSource)
            }
    }

    override fun getDefaultPickupItem(): ItemStack {
        return ItemStack(FinderArrowItem)
    }
}