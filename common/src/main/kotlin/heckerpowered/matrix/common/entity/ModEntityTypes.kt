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
    val MAGIC_LIGHTNING_ENTITY: EntityType<MagicLightningBolt> = register(
        "magic_lightning",
        EntityType.Builder.of({ entityType, world -> MagicLightningBolt(entityType, world) }, MobCategory.MISC)
            .noSave()
            .sized(0.0F, 0.0F)
            .clientTrackingRange(16)
            .updateInterval(Integer.MAX_VALUE)
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

    val FINDER_ARROW_ENTITY: EntityType<FinderArrowEntity> = register(
        "finder_arrow",
        EntityType.Builder.of({ entityType, world -> FinderArrowEntity(world) }, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(20)
    )

    val devEntity: EntityType<DevEntity> = register(
        "dev",
        EntityType.Builder.of({ entityType, world -> DevEntity(world) }, MobCategory.CREATURE)
            .sized(0.6f, 1.95f)
            .eyeHeight(1.74F)
            .clientTrackingRange(8)
    )

    private fun <T : Entity> register(name: String, builder: EntityType.Builder<T>): EntityType<T> {
        val key = ResourceKey.create(Registries.ENTITY_TYPE, Matrix.identifier(name))
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key))
    }

    fun onInitialize() {
        FabricDefaultAttributeRegistry.register(devEntity, DevEntity.createDevAttributes())
    }
}