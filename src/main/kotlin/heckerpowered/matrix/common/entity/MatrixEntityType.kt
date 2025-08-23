/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.entity

import heckerpowered.matrix.Matrix
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object MatrixEntityType {
    val MAGIC_LIGHTNING_ENTITY: EntityType<MagicLightningEntity> = Registry.register(
        Registries.ENTITY_TYPE, Matrix.identifier("magic_lightning"),
        EntityType.Builder.create({ entityType, world -> MagicLightningEntity(entityType, world) }, SpawnGroup.MISC)
            .disableSaving()
            .dimensions(0.0F, 0.0F)
            .maxTrackingRange(16)
            .trackingTickInterval(Integer.MAX_VALUE)
            .build()
    )

    val ATTRACTOR_ENTITY: EntityType<AttractorEntity> = Registry.register(
        Registries.ENTITY_TYPE, Matrix.identifier("attractor"),
        EntityType.Builder.create({ entityType, world -> AttractorEntity(entityType, world) }, SpawnGroup.MISC)
            .makeFireImmune()
            .dimensions(0.98F, 0.98F)
            .eyeHeight(0.15F)
            .maxTrackingRange(10)
            .trackingTickInterval(10)
            .build()
    )

    val FINDER_ARROW_ENTITY: EntityType<FinderArrowEntity> = Registry.register(
        Registries.ENTITY_TYPE, Matrix.identifier("finder_arrow"),
        EntityType.Builder.create({ entityType, world -> FinderArrowEntity(world) }, SpawnGroup.MISC)
            .dimensions(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .maxTrackingRange(4)
            .trackingTickInterval(20)
            .build()
    )

    fun onInitialize() {
    }
}