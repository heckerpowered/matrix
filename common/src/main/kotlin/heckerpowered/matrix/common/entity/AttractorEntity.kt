/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import heckerpowered.matrix.common.network.syncher.ModEntityDataSerializers
import heckerpowered.matrix.core.minus
import heckerpowered.matrix.core.plus
import heckerpowered.matrix.core.times
import heckerpowered.matrix.core.utility.getOtherEntities
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.*
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.*
import kotlin.jvm.optionals.getOrNull

class AttractorEntity(entityType: EntityType<AttractorEntity>, level: Level) : Entity(entityType, level), OwnableEntity {
    companion object {
        val attractionRadius = SynchedEntityData.defineId(AttractorEntity::class.java, ModEntityDataSerializers.double)
        val attractionStrength = SynchedEntityData.defineId(AttractorEntity::class.java, ModEntityDataSerializers.double)
        val attractionDuration = SynchedEntityData.defineId(AttractorEntity::class.java, EntityDataSerializers.INT)
        val ownerUuid = SynchedEntityData.defineId(AttractorEntity::class.java, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE)

        const val ATTRACTION_RADIUS_KEY = "attraction_radius"
        const val ATTRACTION_STRENGTH_KEY = "attraction_strength"
        const val ATTRACTION_DURATION_KEY = "attraction_duration"
    }

    var attractionRadius: Double
        get() = entityData.get(AttractorEntity.attractionRadius)
        set(value) = entityData.set(AttractorEntity.attractionRadius, value)

    var attractionStrength: Double
        get() = entityData.get(AttractorEntity.attractionStrength)
        set(value) = entityData.set(AttractorEntity.attractionStrength, value)

    var attractionDuration: Int
        get() = entityData.get(AttractorEntity.attractionDuration)
        set(value) = entityData.set(AttractorEntity.attractionDuration, value)

    var ownerUuid: EntityReference<LivingEntity>?
        get() = entityData.get(AttractorEntity.ownerUuid).getOrNull()
        set(value) = entityData.set(AttractorEntity.ownerUuid, Optional.ofNullable(value))

    var ownerEntity: LivingEntity?
        get() = super.getOwner()
        set(value) {
            ownerUuid = EntityReference.of(value)
        }

    constructor(level: Level) : this(ModEntityTypes.attractor, level)

    init {
        attractionRadius = 6.0
        attractionStrength = 0.1
        attractionDuration = 20 * 8
    }

    override fun defineSynchedData(entityData: SynchedEntityData.Builder) {
        entityData.define(AttractorEntity.attractionRadius, 6.0)
        entityData.define(AttractorEntity.attractionStrength, 0.1)
        entityData.define(AttractorEntity.attractionDuration, 20 * 8)
        entityData.define(AttractorEntity.ownerUuid, Optional.empty())
    }

    override fun addAdditionalSaveData(output: ValueOutput) {
        output.putDouble(ATTRACTION_RADIUS_KEY, attractionRadius)
        output.putDouble(ATTRACTION_STRENGTH_KEY, attractionStrength)
        output.putInt(ATTRACTION_DURATION_KEY, attractionDuration)

        EntityReference.store(ownerReference, output, "Owner")
    }

    override fun readAdditionalSaveData(input: ValueInput) {
        attractionRadius = input.getDoubleOr(ATTRACTION_RADIUS_KEY, 6.0)
        attractionStrength = input.getDoubleOr(ATTRACTION_STRENGTH_KEY, 0.1)
        attractionDuration = input.getIntOr(ATTRACTION_DURATION_KEY, 20 * 8)

        this@AttractorEntity.ownerUuid = EntityReference.readWithOldOwnerConversion(input, "Owner", level())
    }

    override fun tick() {
        if (attractionDuration-- <= 0) {
            discard()
            return
        }
        if (level().isClientSide) return

        val attractionStrength = this.attractionStrength
        getOtherEntities(attractionRadius).forEach {
            if (it == this) {
                return@forEach
            }

            val direction = position() - it.position()
            val normalizedDirection = direction.normalize()
            val velocity = normalizedDirection * attractionStrength

            it.deltaMovement += velocity
        }
    }

    override fun hurtServer(level: ServerLevel, source: DamageSource, damage: Float): Boolean {
        return false
    }

    override fun shouldRender(camX: Double, camY: Double, camZ: Double): Boolean {
        return false
    }

    override fun canUsePortal(ignorePassenger: Boolean): Boolean {
        return false
    }

    override fun restoreFrom(oldEntity: Entity) {
        super.restoreFrom(oldEntity)
        if (oldEntity is AttractorEntity) {
            this.ownerUuid = oldEntity.ownerUuid
        }
    }

    override fun getOwnerReference(): EntityReference<LivingEntity>? {
        return this@AttractorEntity.ownerUuid
    }
}
