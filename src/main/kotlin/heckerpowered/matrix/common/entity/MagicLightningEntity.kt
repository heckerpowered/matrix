/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.common.effect.MatrixStatusEffects.ARMOR_PENETRATION_EFFECT
import heckerpowered.matrix.common.effect.MatrixStatusEffects.CRIPPLE_MOVEMENT_EFFECT
import heckerpowered.matrix.common.effect.MatrixStatusEffects.EXPOSED_EFFECT
import heckerpowered.matrix.common.entity.MagicLightningEntity.LightningType.*
import heckerpowered.matrix.common.magic.ChannelExecutor
import heckerpowered.matrix.common.magic.ChannelRequest
import heckerpowered.matrix.common.magic.spell.CrippleMovementMagic
import heckerpowered.matrix.common.magic.spell.ExplosionMagic.explosionBehavior
import heckerpowered.matrix.common.magic.spell.LightningBoltMagic
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.block.AbstractFireBlock
import net.minecraft.block.Blocks
import net.minecraft.block.LightningRodBlock
import net.minecraft.block.Oxidizable
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.server.network.EntityTrackerEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.Difficulty
import net.minecraft.world.GameRules
import net.minecraft.world.World
import net.minecraft.world.WorldEvents
import net.minecraft.world.event.GameEvent
import java.util.*

class MagicLightningEntity(entityType: EntityType<MagicLightningEntity>, world: World) : Entity(entityType, world) {

    private var ambientTick = 2
    private var remainingActions = 0

    var seed = 0L
    var lightningType = NORMAL

    /**
     * Represents different lightning colors used in bloom effects.
     *
     * All colors (except [BLACK]) have been adjusted so that their
     * perceived brightness (NIST relative luminance) is approximately
     * equal to [NORMAL]. This ensures that during bloom rendering,
     * each color contributes with a consistent brightness level while
     * still preserving its hue and saturation.
     *
     * [BLACK] remains absolute dark with zero luminance.
     */
    enum class LightningType(val color: Color) {
        NORMAL(Color(114, 114, 128, 255)),
        RED(Color(228, 24, 24, 255)),
        ORANGE(Color(181, 90, 24, 255)),
        YELLOW(Color(119, 119, 24, 255)),
        GREEN(Color(24, 133, 24, 255)),
        CYAN(Color(24, 128, 128, 255)),
        BLUE(Color(92, 92, 255, 255)),
        PURPLE(Color(200, 24, 200, 255)),
        BLACK(Color(0, 0, 0, 255))
    }

    var channeler: ServerPlayerEntity? = null

    constructor(world: World) : this(MatrixEntityType.MAGIC_LIGHTNING_ENTITY, world)

    init {
        seed = random.nextLong()
        remainingActions = random.nextInt(3) + 1
    }

    private val affectedBlockPos
        get() = BlockPos.ofFloored(pos.x, pos.y - Double.MIN_VALUE, pos.z)

    private fun playSound() {
        if (!world.isClient) {
            return
        }

        world.playSound(x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000F, 0.8F + random.nextFloat() * 0.2F, false)
        world.playSound(x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0f, 0.5F + random.nextFloat() * 0.2F, false)
    }

    private fun strikeBlocks() {
        val difficulty = world.difficulty
        if (difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD) {
            spawnFire(4)
            powerLightningRod()
            cleanOxidation(affectedBlockPos)
            emitGameEvent(GameEvent.LIGHTNING_STRIKE)
        }
    }

    private fun placeFireAt(blockPos: BlockPos) {
        val fireBlockState = AbstractFireBlock.getState(world, blockPos)
        if (world.getBlockState(blockPos).isAir && fireBlockState.canPlaceAt(world, blockPos)) {
            world.setBlockState(blockPos, fireBlockState)
        }
    }

    private fun spawnFire(spreadAttempts: Int) {
        if (world.isClient || !world.gameRules.getBoolean(GameRules.DO_FIRE_TICK)) {
            return
        }

        placeFireAt(blockPos)
        repeat(spreadAttempts) {
            val spreadBlockPos = blockPos.add(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1)
            placeFireAt(spreadBlockPos)
        }
    }

    private fun cleanOxidation(blockPos: BlockPos) {
        val blockState = world.getBlockState(blockPos)
        val oxidizableBlockPos = if (blockState.isOf(Blocks.LIGHTNING_ROD)) {
            blockPos.offset(blockState.get(LightningRodBlock.FACING).opposite)
        } else {
            blockPos
        }
        val oxidizableBlockState = world.getBlockState(oxidizableBlockPos)
        if (oxidizableBlockState.block is Oxidizable) {
            world.setBlockState(oxidizableBlockPos, Oxidizable.getUnaffectedOxidationState(oxidizableBlockState))

            val mutableBlockPos = blockPos.mutableCopy()
            val cleanCount = world.random.nextInt(3) + 3
            repeat(cleanCount) {
                val subCleanCount = world.random.nextInt(8) + 1
                cleanOxidationAround(blockPos, mutableBlockPos, subCleanCount)
            }
        }
    }

    private fun cleanOxidationAround(pos: BlockPos, mutablePos: BlockPos.Mutable, count: Int) {
        mutablePos.set(pos)

        repeat(count) {
            val optional = cleanOxidationAround(mutablePos)
            if (optional.isEmpty) {
                return@repeat
            }

            mutablePos.set(optional.get())
        }
    }

    private fun cleanOxidationAround(blockPos: BlockPos): Optional<BlockPos> {
        for (randomBlockPos in BlockPos.iterateRandomly(world.random, 10, blockPos, 1)) {
            val blockState = world.getBlockState(randomBlockPos)
            if (blockState.block !is Oxidizable) {
                continue
            }

            Oxidizable.getDecreasedOxidationState(blockState).ifPresent { world.setBlockState(randomBlockPos, it) }
            world.syncWorldEvent(WorldEvents.ELECTRICITY_SPARKS, randomBlockPos, -1)
            return Optional.of(randomBlockPos)
        }

        return Optional.empty()
    }

    private fun powerLightningRod() {
        val affectedBlockState = world.getBlockState(affectedBlockPos)
        val affectedBlock = affectedBlockState.block
        if (affectedBlock is LightningRodBlock) {
            affectedBlock.setPowered(affectedBlockState, world, affectedBlockPos)
        }
    }

    private fun doRemainingActions() {
        if (remainingActions == 0) {
            discard()
        } else if (ambientTick < -random.nextInt(10)) {
            remainingActions--
            ambientTick = 1
            seed = random.nextLong()
            spawnFire(0)
        }
    }

    private fun strikeEntities() {
        if (world !is ServerWorld) {
            world.setLightningTicksLeft(2)
        }

        val range = if (lightningType == PURPLE) {
            6.0
        } else {
            3.0
        }

        val entities = world.getOtherEntities(
            this,
            Box(
                x - range, y - range, z - range,
                x + range, y + range + 6.0, z + range
            )
        ) { it.isAlive }

        for (entity in entities) {
            onStruckByLightning(entity)
        }
    }

    private fun onStruckByLightning(entity: Entity) {
        ++entity.fireTicks
        if (entity.fireTicks == 0) {
            entity.setOnFireFor(8.0f)
        }

        val channeler = this.channeler
        val damageSource = if (channeler != null) {
            channeler.world.damageSources.create(MatrixDamageTypes.magic, channeler)
        } else {
            damageSources.generic()
        }
        when (lightningType) {
            NORMAL -> entity.damage(damageSource, 5.0F)

            RED -> entity.damage(damageSource, 20.0F)
            ORANGE -> {
                if (entity is LivingEntity) {
                    entity.addStatusEffect(StatusEffectInstance(ARMOR_PENETRATION_EFFECT, 20 * 10, 4))
                }
                entity.damage(damageSource, 5.0F)
            }

            YELLOW -> {
                channeler?.apply {
                    lastAttackedTicks = Int.MAX_VALUE
                    swingHand(activeHand)
                    attack(entity)
                    addCritParticles(entity)
                    addEnchantedHitParticles(entity)
                }
                entity.damage(damageSource, 5.0F)
                if (entity is LivingEntity) {
                    entity.addStatusEffect(StatusEffectInstance(StatusEffects.GLOWING, 20 * 10, 0))
                }
            }

            GREEN -> {
                channeler?.heal(2F)
                entity.damage(damageSource, 5.0F)
                if (entity is LivingEntity) {
                    entity.addStatusEffect(StatusEffectInstance(StatusEffects.POISON, 20 * 10, 4))
                }
            }

            CYAN -> {
                if (entity is LivingEntity) {
                    if (entity.hasStatusEffect(CRIPPLE_MOVEMENT_EFFECT)) {
                        entity.addStatusEffect(StatusEffectInstance(EXPOSED_EFFECT, 20 * 10, 0))
                    }
                    val channeler = channeler
                    if (channeler == null) {
                        entity.addStatusEffect(StatusEffectInstance(CRIPPLE_MOVEMENT_EFFECT, 20 * 10, 4))
                    } else {
                        ChannelExecutor.channel(CrippleMovementMagic, channeler, entity, ChannelRequest(costMana = false))
                    }
                }
            }

            BLUE -> {
                if (channeler == null) {
                    entity.damage(damageSource, 5.0F)
                    return
                }

                for (target in entity.world.getOtherEntities(entity, entity.boundingBox.expand(6.0))) {
                    if (target !is LivingEntity) {
                        continue
                    }

                    ChannelExecutor.channel(LightningBoltMagic, channeler, target)
                }
            }

            PURPLE -> {
                entity.damage(damageSource, 5F)
                world.createExplosion(entity, damageSource, explosionBehavior, entity.x, entity.y, entity.z, 1.0F, false, World.ExplosionSourceType.MOB)
                // TODO: add status effect
            }

            BLACK -> {
                entity.kill()
            }
        }
    }

    override fun tick() {
        super.tick()
        if (ambientTick == 2) {
            playSound()
            strikeBlocks()
        }

        --ambientTick
        if (ambientTick < 0) {
            doRemainingActions()
        } else {
            strikeEntities()
        }
    }

    override fun shouldRender(distance: Double): Boolean {
        val maxDistance = 64.0 * getRenderDistanceMultiplier()
        return distance < maxDistance * maxDistance
    }

    override fun getSoundCategory() = SoundCategory.WEATHER

    override fun initDataTracker(builder: DataTracker.Builder) {}
    override fun readCustomDataFromNbt(nbt: NbtCompound) {}
    override fun writeCustomDataToNbt(nbt: NbtCompound) {}

    override fun onSpawnPacket(packet: EntitySpawnS2CPacket) {
        super.onSpawnPacket(packet)
        val lightningTypeOrdinal = packet.entityData
        val lightningTypes = LightningType.entries.toTypedArray()
        if (lightningTypeOrdinal in lightningTypes.indices) {
            lightningType = lightningTypes[lightningTypeOrdinal]
        }
    }

    override fun createSpawnPacket(entityTrackerEntry: EntityTrackerEntry?): Packet<ClientPlayPacketListener> {
        return EntitySpawnS2CPacket(this, entityTrackerEntry, lightningType.ordinal)
    }
}