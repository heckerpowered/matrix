/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.channel

import net.minecraft.entity.LivingEntity

/**
 * Represents an object that has an associated [LivingEntity].
 *
 * This interface is designed to be implemented by classes that need to associate
 * a [LivingEntity] with their instance, typically for the purpose of linking game
 * mechanics or behaviors directly to in-game entities.
 *
 * @property entity The [LivingEntity] associated with the implementing object.
 */
interface HasEntity {
    val entity: LivingEntity
}