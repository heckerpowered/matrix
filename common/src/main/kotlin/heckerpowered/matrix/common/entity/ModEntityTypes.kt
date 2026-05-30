/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import heckerpowered.matrix.Matrix
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory


object ModEntityTypes {
    val MAGIC_LIGHTNING_ENTITY: EntityType<MagicLightningBolt> = Registry.register(
        Registries.ENTITY_TYPE, Matrix.identifier("magic_lightning"),
        EntityType.Builder.create({ entityType, world -> MagicLightningBolt(entityType, world) }, SpawnGroup.MISC)
            .disableSaving()
            .dimensions(0.0F, 0.0F)
            .maxTrackingRange(16)
            .trackingTickInterval(Integer.MAX_VALUE)
            .build()
    )

    val attractor = register(
        "attractor",
        EntityType.Builder.of(::AttractorEntity, MobCategory.MISC)
            .fireImmune()
            .sized(0.98F, 0.98F)
            .eyeHeight(0.15F)
            .clientTrackingRange(10)
            .updateInterval(10)
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

    val devEntity: EntityType<DevEntity> = Registry.register(
        Registries.ENTITY_TYPE, Matrix.identifier("dev"),
        EntityType.Builder.create({ entityType, world -> DevEntity(world) }, SpawnGroup.CREATURE)
            .dimensions(0.6f, 1.95f)
            .eyeHeight(1.74F)
            .maxTrackingRange(8)
            .build()
    )

    private fun <T : Entity> register(name: String, builder: EntityType.Builder<T>): EntityType<T> {
        val key = ResourceKey.create(Registries.ENTITY_TYPE, Matrix.identifier(name))
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key))
    }

    fun onInitialize() {
        FabricDefaultAttributeRegistry.register(devEntity, DevEntity.createDevAttributes())
    }
}