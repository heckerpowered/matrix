package heckerpowered.matrix.common.entity

import heckerpowered.matrix.Matrix
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object MatrixEntityType {
    val magicLightningEntity: EntityType<MagicLightningEntity> = Registry.register(
        Registries.ENTITY_TYPE, Matrix.identifier("magic_lightning"),
        EntityType.Builder.create({ entityType, world -> MagicLightningEntity(entityType, world) }, SpawnGroup.MISC)
            .disableSaving()
            .dimensions(0.0F, 0.0F)
            .maxTrackingRange(16)
            .trackingTickInterval(Integer.MAX_VALUE)
            .build()
    )

    fun onInitialize() {
    }
}