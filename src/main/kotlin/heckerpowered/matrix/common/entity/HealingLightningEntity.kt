package heckerpowered.matrix.common.entity

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LightningEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.nbt.NbtCompound
import net.minecraft.particle.ParticleTypes
import net.minecraft.world.World

/**
 *
 */
class HealLightningEntity(entityType: EntityType<out LightningEntity>, world: World) :
    MatrixLightningEntity(entityType, world) {
    companion object {
        private val HEAL_AMOUNT =
            DataTracker.registerData(HealLightningEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
    }

    private var healAmount: Float
        get() = dataTracker.get(HEAL_AMOUNT)
        set(value) = dataTracker.set(HEAL_AMOUNT, value)

    override val color: LightningColor
        get() = BuiltinLightningColors.GREEN

    override fun onStruckEntity(entity: Entity) {
        super.onStruckEntity(entity)
        if (entity is LivingEntity) {
            entity.heal(healAmount)
            world.addParticle(ParticleTypes.HEART, entity.pos.x, entity.pos.y, entity.pos.z, .0, 1.0, .0)
        }
    }

    override fun initDataTracker() {
        super.initDataTracker()
        dataTracker.startTracking(HEAL_AMOUNT, 4.0f)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        healAmount = nbt.getFloat("HealAmount")
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putFloat("HealAmount", healAmount)
    }
}