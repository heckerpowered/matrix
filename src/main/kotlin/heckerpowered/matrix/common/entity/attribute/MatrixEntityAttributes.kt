package heckerpowered.matrix.common.entity.attribute

import heckerpowered.matrix.Matrix
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.mob.*
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.world.Difficulty


object MatrixEntityAttributes {
    @JvmField
    val MAGIC_RESISTANCE = register("magic_resistance", ClampedEntityAttribute("attribute.matrix.magic_resistance", .0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))

    fun onInitialize() {
        FabricDefaultAttributeRegistry.register(EntityType.VEX, VexEntity.createVexAttributes().add(MAGIC_RESISTANCE, -0.25))
        FabricDefaultAttributeRegistry.register(EntityType.WITHER_SKELETON, WitherSkeletonEntity.createAbstractSkeletonAttributes().add(MAGIC_RESISTANCE, 0.1))
        FabricDefaultAttributeRegistry.register(EntityType.GUARDIAN, GuardianEntity.createGuardianAttributes().add(MAGIC_RESISTANCE, 0.15))
        FabricDefaultAttributeRegistry.register(EntityType.ELDER_GUARDIAN, ElderGuardianEntity.createElderGuardianAttributes().add(MAGIC_RESISTANCE, 0.4))
        FabricDefaultAttributeRegistry.register(EntityType.ENDERMAN, EndermanEntity.createEndermanAttributes().add(MAGIC_RESISTANCE, 0.2))
        FabricDefaultAttributeRegistry.register(EntityType.EVOKER, EvokerEntity.createEvokerAttributes().add(MAGIC_RESISTANCE, 0.85))
        FabricDefaultAttributeRegistry.register(EntityType.WITCH, WitchEntity.createWitchAttributes().add(MAGIC_RESISTANCE, 0.85))
        FabricDefaultAttributeRegistry.register(EntityType.WITHER, WitherEntity.createWitherAttributes().add(MAGIC_RESISTANCE, 1.0))
        FabricDefaultAttributeRegistry.register(EntityType.ENDER_DRAGON, EnderDragonEntity.createEnderDragonAttributes().add(MAGIC_RESISTANCE, 1.0))
        FabricDefaultAttributeRegistry.register(EntityType.WARDEN, WardenEntity.createHostileAttributes().add(MAGIC_RESISTANCE, 2.0))
    }

    private fun register(name: String, attribute: EntityAttribute): RegistryEntry<EntityAttribute> {
        return Registry.registerReference(Registries.ATTRIBUTE, Matrix.identifier(name), attribute)
    }

    val LivingEntity.manaResistance: Double
        get() = getAttributeInstance(MAGIC_RESISTANCE)?.value ?: .0

    val LivingEntity.adjustedManaResistance: Double
        get() = manaResistance * when (world.difficulty) {
            Difficulty.PEACEFUL -> .0 // -100% mana resistance
            Difficulty.EASY -> 0.6 // -40% mana resistance
            Difficulty.NORMAL -> 1.0
            Difficulty.HARD -> 1.4 // + 40% mana resistance
        }
}