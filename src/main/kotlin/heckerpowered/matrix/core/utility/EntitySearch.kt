/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.core.utility

import heckerpowered.matrix.core.*
import net.minecraft.entity.Entity
import net.minecraft.world.World
import kotlin.math.acos

object EntitySearch {
    fun Entity.getEntitiesNearSight(maxDistance: Double, fieldOfViewDegrees: Double, tickDelta: Float = 1F): List<Entity> {
        val rotationVector = getRotationVec(tickDelta)
        val position = eyePos

        val searchBox = position.toBox().expand(maxDistance)
        val entities = world.getOtherEntities(this, searchBox) { true }

        return entities.asSequence()
            .map { entity ->
                val entityCenterPosition = entity.boundingBox.center

                // Direction from `this` to the searched entity
                val normalizedTargetDirection = (entityCenterPosition - position).normalize()
                val dotProduct = rotationVector.dotProduct(normalizedTargetDirection)
                val angleRadians = acos(dotProduct.coerceIn(-1.0, 1.0))
                val distance = position distanceTo entityCenterPosition
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
            .toList()
    }

    val World.entitySequence: Sequence<Entity>
        get() = entityLookup.iterate().asSequence()

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
            return entity.world.getOtherEntities(entity, entity.boundingBox.expand(maxDistance))
                .filter { it !in visitedEntities }
                .minByOrNull { it squaredDistanceTo entity }
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
        return world
            .getOtherEntities(this, pos.toBox().expand(maxDistance))
            .asSequence()
            .sortedBy { squaredDistanceTo(it) }
    }
}