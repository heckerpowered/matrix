/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.world.World

class DevEntity(entityType: EntityType<out DevEntity>, world: World) : PathAwareEntity(entityType, world) {

    constructor(world: World) : this(MatrixEntityType.devEntity, world)

    companion object {
        const val HEALTH_SCALE = 10000000.0

        fun createDevAttributes(): DefaultAttributeContainer.Builder {
            return createMobAttributes()
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 20.0)
                .add(EntityAttributes.GENERIC_ARMOR, 20.0)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 2.0)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1000.0 * HEALTH_SCALE)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS)
        }
    }

    override fun damage(source: DamageSource, amount: Float): Boolean {
        return super.damage(source, amount)
    }

    override fun getAttributeValue(attribute: RegistryEntry<EntityAttribute>): Double {
        if (attribute == EntityAttributes.GENERIC_MAX_HEALTH) {
            return 1000.0 * HEALTH_SCALE
        }
        return super.getAttributeValue(attribute)
    }
}