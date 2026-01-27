/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import heckerpowered.matrix.common.entity.data.MatrixTrackedDataHandlerRegistry.DOUBLE
import heckerpowered.matrix.core.minus
import heckerpowered.matrix.core.plus
import heckerpowered.matrix.core.times
import heckerpowered.matrix.core.toBox
import net.minecraft.entity.*
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.DataTracker.registerData
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.World

class AttractorEntity(entityType: EntityType<AttractorEntity>, world: World) : Entity(entityType, world), Ownable {
    companion object {
        val attractionRadius: TrackedData<Double> = registerData(AttractorEntity::class.java, DOUBLE)
        val attractionStrength: TrackedData<Double> = registerData(AttractorEntity::class.java, DOUBLE)
        val attractionDuration: TrackedData<Int> = registerData(AttractorEntity::class.java, TrackedDataHandlerRegistry.INTEGER)

        const val ATTRACTION_RADIUS_KEY = "attraction_radius"
        const val ATTRACTION_STRENGTH_KEY = "attraction_strength"
        const val ATTRACTION_DURATION_KEY = "attraction_duration"
    }

    var attractionRadius: Double
        get() = dataTracker.get(AttractorEntity.attractionRadius)
        set(value) = dataTracker.set(AttractorEntity.attractionRadius, value)

    var attractionStrength: Double
        get() = dataTracker.get(AttractorEntity.attractionStrength)
        set(value) = dataTracker.set(AttractorEntity.attractionStrength, value)

    var attractionDuration: Int
        get() = dataTracker.get(AttractorEntity.attractionDuration)
        set(value) = dataTracker.set(AttractorEntity.attractionDuration, value)

    var owner: LivingEntity? = null

    constructor(world: World) : this(MatrixEntityType.ATTRACTOR_ENTITY, world)

    init {
        attractionRadius = 6.0
        attractionStrength = 0.1
        attractionDuration = 20 * 8
    }

    override fun initDataTracker(builder: DataTracker.Builder) {
        builder.add(AttractorEntity.attractionRadius, 6.0)
        builder.add(AttractorEntity.attractionStrength, 0.1)
        builder.add(AttractorEntity.attractionDuration, 20 * 8)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        attractionRadius = nbt.getDouble(ATTRACTION_RADIUS_KEY)
        attractionStrength = nbt.getDouble(ATTRACTION_STRENGTH_KEY)
        attractionDuration = nbt.getInt(ATTRACTION_DURATION_KEY)
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        nbt.putDouble(ATTRACTION_RADIUS_KEY, attractionRadius)
        nbt.putDouble(ATTRACTION_STRENGTH_KEY, attractionStrength)
        nbt.putInt(ATTRACTION_DURATION_KEY, attractionDuration)
    }

    override fun tick() {
        if (attractionDuration-- <= 0) {
            discard()
            return
        }
        if (world.isClient) {
            return
        }

        val attractionStrength = this.attractionStrength
        world.getOtherEntities(owner, pos.toBox().expand(attractionRadius)).forEach {
            if (it == this) {
                return@forEach
            }

            val direction = pos - it.pos
            val normalizedDirection = direction.normalize()
            val velocity = normalizedDirection * attractionStrength

            it.velocity += velocity
            it.velocityModified = true
        }
    }

    override fun shouldRender(distance: Double): Boolean {
        return false
    }

    override fun canUsePortals(allowVehicles: Boolean): Boolean {
        return false
    }

    override fun copyFrom(original: Entity?) {
        super.copyFrom(original)
        if (original is TntEntity) {
            this.owner = original.owner
        }
    }

    override fun getOwner(): Entity? {
        return owner
    }
}