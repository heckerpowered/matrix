package heckerpowered.matrix.common.entity

import heckerpowered.matrix.common.entity.MatrixEntityType.FINDER_ARROW_ENTITY
import heckerpowered.matrix.common.item.FinderArrowItem
import heckerpowered.matrix.core.squaredDistanceTo
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class FinderArrowEntity : PersistentProjectileEntity {
    constructor(world: World, owner: LivingEntity, stack: ItemStack, shotFrom: ItemStack?) : super(FINDER_ARROW_ENTITY, owner, world, stack, shotFrom)
    constructor(world: World) : super(FINDER_ARROW_ENTITY, world)

    override fun tick() {
        super.tick()
        if (world.isClient && !inGround) {
            world.addParticle(ParticleTypes.INSTANT_EFFECT, x, y, z, 0.0, 0.0, 0.0)
        }

        if (inGround) {
            return
        }
        val previousPosition = Vec3d(prevX, prevY, prevZ)
        val currentPosition = pos
        val searchBox = Box(previousPosition, currentPosition).expand(12.0, 9999.0, 12.0)
        world.getOtherEntities(owner, searchBox)
            .filterIsInstance<LivingEntity>()
            .filter {
                val blockPos = BlockPos.ofFloored(it.x, it.y, it.z)
                this squaredDistanceTo it <= 144 // 144 = 12 * 12 (radius)
                        || it.world.isSkyVisible(blockPos)
            }
            .forEach {
                val statusEffectInstance = StatusEffectInstance(StatusEffects.GLOWING, 20 * 5, 0)
                it.addStatusEffect(statusEffectInstance, effectCause)
            }
    }

    override fun getDefaultItemStack(): ItemStack {
        return ItemStack(FinderArrowItem)
    }
}