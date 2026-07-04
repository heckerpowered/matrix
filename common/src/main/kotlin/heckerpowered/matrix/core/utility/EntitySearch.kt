/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.utility

import heckerpowered.matrix.core.*
import heckerpowered.matrix.mixin.EntitySectionAccessor
import heckerpowered.matrix.mixin.EntitySectionStorageAccessor
import heckerpowered.matrix.mixin.LevelEntityGetterAdapterAccessor
import net.minecraft.core.SectionPos
import net.minecraft.util.ClassInstanceMultiMap
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart
import net.minecraft.world.level.Level
import net.minecraft.world.level.entity.EntityAccess
import net.minecraft.world.level.entity.EntitySection
import net.minecraft.world.level.entity.EntitySectionStorage
import net.minecraft.world.level.entity.LevelEntityGetterAdapter
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.acos

fun Entity.getEntitiesNearSight(maxDistance: Double, fieldOfViewDegrees: Double, tickDelta: Float = 1F): Sequence<Entity> {
    val rotationVector = getViewVector(tickDelta)
    val position = getEyePosition(tickDelta)

    return getOtherEntities(maxDistance)
        .map { entity ->
            val entityCenterPosition = entity.boundingBox.center

            // Direction from `this` to the searched entity
            val normalizedTargetDirection = (entityCenterPosition - position).normalize()
            val dotProduct = rotationVector.dot(normalizedTargetDirection)
            val angleRadians = acos(dotProduct.coerceIn(-1.0, 1.0))
            val distance = position.distanceTo(entityCenterPosition)
            Triple(entity, angleRadians, distance)
        }
        .filter { (_, angleRadians, distance) ->
            val angleDegrees = toDegrees(angleRadians)
            angleDegrees <= fieldOfViewDegrees && distance <= maxDistance
        }
        .sortedBy { (_, angleRad, _) -> angleRad }
        .sortedWith(
            // Sort entities first by angle to the view direction, then by distance
            compareBy({ it.second }, { it.third })
        )
        .map { it.first }
}

val Level.entities: Sequence<Entity>
    get() = entityGetter.all.asSequence()

/**
 * Builds a chain of spatially adjacent entities, starting from the specified root entity.
 *
 * From the initial `start` entity, repeatedly finds the nearest entity (not yet visited)
 *
 * @param maxDistance The maximum distance to consider for the next entity in the chain.
 * @return A list of entities forming the chain, in order of traversal.
 */
fun Entity.getAdjacentEntities(maxDistance: Double): Sequence<Entity> = sequence {
    val visitedEntities = mutableSetOf<Entity>()
    var current: Entity? = this@getAdjacentEntities

    fun getNearestUnvisitedEntity(entity: Entity): Entity? {
        return entity.getOtherEntities(maxDistance)
            .filter { it !in visitedEntities }
            .minByOrNull { it.distanceToSqr(entity) }
    }
    while (current != null) {
        yield(current)
        visitedEntities.add(current)
        current = getNearestUnvisitedEntity(current)
    }
}

/**
 * Returns a lazily evaluated [Sequence] of entities that are spatially adjacent to this entity,
 * within a given maximum distance.
 *
 * The returned entities are located within a spherical bounding region defined by [maxDistance],
 * and are sorted in ascending order of squared distance from this entity.
 * This entity (`this`) is excluded from the result.
 *
 * @param maxDistance The maximum distance to consider when identifying adjacent entities.
 * @return A sequence of entities ordered from nearest to farthest, lazily evaluated.
 */
fun Entity.getNearestEntities(maxDistance: Double): Sequence<Entity> {
    return getOtherEntities(maxDistance)
        .sortedBy { distanceToSqr(it) }
}

/**
 * Returns a lazily evaluated [Sequence] of entities within a given maximum distance.
 *
 * The returned entities are located within a spherical bounding region defined by [maxDistance].
 * This entity (`this`) is excluded from the result.
 *
 * @param maxDistance The maximum distance
 * @return A sequence of entities, unordered, lazily evaluated.
 */
fun Entity.getOtherEntities(maxDistance: Double): Sequence<Entity> {
    val searchBox = position().toAABB().inflate(maxDistance)
    return level()
        .getEntities(searchBox)
        .filter { it !== this }
        .filter { it !is EnderDragonPart || it.parentMob !== this }
}

fun <T : Entity> Sequence<T>.withinDistance(center: Vec3, maxDistance: Double): Sequence<T> {
    val maxDistanceSquared = maxDistance * maxDistance
    return filter { entity ->
        entity.distanceToSqr(center) <= maxDistanceSquared
    }
}

fun <T : Entity> Sequence<T>.withinDistance(origin: Entity, maxDistance: Double): Sequence<T> =
    withinDistance(origin.position(), maxDistance)

fun <T : Entity> Iterable<T>.withinDistance(center: Vec3, maxDistance: Double): List<T> {
    val maxDistanceSquared = maxDistance * maxDistance
    return filter { entity ->
        entity.distanceToSqr(center) <= maxDistanceSquared
    }
}

fun <T : Entity> Iterable<T>.withinDistance(origin: Entity, maxDistance: Double): List<T> =
    withinDistance(origin.position(), maxDistance)

fun Level.getEntities(searchBox: AABB): Sequence<Entity> = sequence {
    val entityGetter = entityGetter
    if (entityGetter !is LevelEntityGetterAdapter<*>) {
        yieldAll(getEntities(null, searchBox) { true })
        return@sequence
    }

    @Suppress("UNCHECKED_CAST")
    val sectionStorage = (entityGetter as LevelEntityGetterAdapterAccessor)
        .`matrix$getSectionStorage`() as EntitySectionStorage<EntityAccess>
    val entities = sectionStorage
        .getEntities(searchBox)
        .mapNotNull { it as? Entity }

    val dragonParts = dragonParts()
        .asSequence()
        .filter { searchBox.intersects(it.boundingBox) }

    yieldAll(entities)
    yieldAll(dragonParts)
}

fun <T : EntityAccess> EntitySectionStorage<T>.getEntities(searchBox: AABB): Sequence<T> =
    getAccessibleNonEmptySections(searchBox).flatMap { it.getEntities(searchBox) }

fun <T : EntityAccess> EntitySectionStorage<T>.getAccessibleNonEmptySections(
    searchBox: AABB,
): Sequence<EntitySection<T>> = sequence {
    // Match vanilla's broad-phase entity section search range.
    // This intentionally expands the box to avoid missing nearby entities
    // around section boundaries.
    val minSectionX = SectionPos.posToSectionCoord(searchBox.minX - 2.0)
    val minSectionY = SectionPos.posToSectionCoord(searchBox.minY - 4.0)
    val minSectionZ = SectionPos.posToSectionCoord(searchBox.minZ - 2.0)
    val maxSectionX = SectionPos.posToSectionCoord(searchBox.maxX + 2.0)
    val maxSectionY = SectionPos.posToSectionCoord(searchBox.maxY + 0.0)
    val maxSectionZ = SectionPos.posToSectionCoord(searchBox.maxZ + 2.0)

    for (sectionX in minSectionX..maxSectionX) {
        // Sections packs x/y/z into a single long key.
        // For a fixed x, all (x, *, *) keys form one contiguous range.
        // (x, 0, 0) is the smallest key in that range.
        // (x, -1, -1) is the largest because -1 is all bits set,
        // which fills the packed y/z bitfields with their maximum values.
        val minSectionKey = SectionPos.asLong(sectionX, 0, 0)
        val maxSectionKey = SectionPos.asLong(sectionX, -1, -1)

        val sections = (this@getAccessibleNonEmptySections as EntitySectionStorageAccessor)
            .`matrix$getSectionIds`()
            .subSet(minSectionKey, maxSectionKey + 1L)
            .asSequence()
            .filter { sectionKey ->
                val sectionY = SectionPos.y(sectionKey)
                val sectionZ = SectionPos.z(sectionKey)
                sectionY in minSectionY..maxSectionY && sectionZ in minSectionZ..maxSectionZ
            }
            .mapNotNull(::getSection)
            .filter { !it.isEmpty && it.status.isAccessible }

        yieldAll(sections)
    }
}

fun <T : EntityAccess> EntitySection<T>.getEntities(searchBox: AABB): Sequence<T> {
    @Suppress("UNCHECKED_CAST")
    val storage = (this as EntitySectionAccessor).`matrix$getStorage`() as ClassInstanceMultiMap<T>
    return storage.asSequence().filter { it.boundingBox.intersects(searchBox) }
}