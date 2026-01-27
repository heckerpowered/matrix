/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.event.ReadDataCallback
import heckerpowered.matrix.common.event.WriteDataCallback
import heckerpowered.matrix.common.magic.*
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.magic.channel.ChannelExecutor
import heckerpowered.matrix.common.magic.channel.ChannelQueue
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.channel.ChannelRequest
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicAvailableStatus
import heckerpowered.matrix.common.magic.core.MagicDataSpecification
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.network.ExplosionPayload
import heckerpowered.matrix.core.common.balance.Accumulator
import heckerpowered.matrix.core.extensions.EntityExtensions.damage
import heckerpowered.matrix.core.killed
import heckerpowered.matrix.core.utility.EntitySearch.getNearestEntities
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.mob.WardenEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld

object SculkCatalystMagic : Magic(
    MagicDefinition(
        Matrix.identifier("sculk_catalyst"),
        12.mana,
        (9 * 20).ticks
    )
), MagicDataSpecification {

    @Serializable
    private class SculkCatalystExecutionPayload(
        var bounces: Long = 0,
    ) : heckerpowered.matrix.common.magic.core.ExecutionPayload()

    private class SculkCatalystChannelRequest(
        bypassLock: Boolean = false,
        costMana: Boolean = true,
        data: heckerpowered.matrix.common.magic.core.ExecutionPayload = _root_ide_package_.heckerpowered.matrix.common.magic.core.ExecutionPayload(),
    ) : ChannelRequest(bypassLock, costMana, data) {
        override fun isMagicAvailable(availableStatus: MagicAvailableStatus): Boolean {
            if (availableStatus == MagicAvailableStatus.SCULK_CATALYST_IS_ALREADY_ACTIVE) {
                return true
            }
            return super.isMagicAvailable(availableStatus)
        }
    }

    init {
        WriteDataCallback.EVENT.register(::onWriteData)
        ReadDataCallback.EVENT.register(::onReadData)
    }

    private fun onWriteData(entity: LivingEntity, nbt: NbtCompound) {
        if (entity !is PlayerEntity) return

        val trackedEntities = sculkCatalystTracker[entity] ?: return
        val idList = NbtList()
        for (tracked in trackedEntities) {
            idList.add(NbtInt.of(tracked.id))
        }

        val matrixNbt = NbtCompound()
        matrixNbt.put("TrackedEntityIds", idList)
        nbt.put("MatrixMod", matrixNbt)
    }

    private fun onReadData(entity: LivingEntity, nbt: NbtCompound) {
        if (entity !is PlayerEntity) {
            return
        }
        val world = entity.world
        if (!nbt.contains("MatrixMod", NbtElement.COMPOUND_TYPE.toInt())) {
            return
        }
        val matrixNbt = nbt.getCompound("MatrixMod")

        if (!matrixNbt.contains("TrackedEntityIds", NbtElement.LIST_TYPE.toInt())) {
            return
        }
        val idList = matrixNbt.getList("TrackedEntityIds", NbtElement.INT_TYPE.toInt())

        val resolvedEntities = mutableListOf<LivingEntity>()
        for (element in idList) {
            val id = (element as? NbtInt)?.intValue() ?: continue
            val tracked = world.getEntityById(id) as? LivingEntity
            if (tracked != null) {
                resolvedEntities.add(tracked)
            }
        }

        sculkCatalystTracker[entity] = resolvedEntities
    }

    private val sculkCatalystTracker = mutableMapOf<PlayerEntity, MutableList<LivingEntity>>()
    private val lock = Any()

    override fun channel(player: PlayerEntity, target: LivingEntity, queue: ChannelQueue, data: heckerpowered.matrix.common.magic.core.ExecutionPayload) {
        super.channel(player, target, queue, data)
        synchronized(lock) {
            sculkCatalystTracker.computeIfAbsent(player) {
                mutableListOf()
            }.add(target)
        }
    }

    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: heckerpowered.matrix.common.magic.core.ExecutionPayload) {
        super.cast(player, target, sequence, data)
        val sculkCatalystData = data as? SculkCatalystExecutionPayload ?: SculkCatalystExecutionPayload()
        sculkCatalystData.copyFrom(data)
        val bounces = ++sculkCatalystData.bounces

        val damageSource = MemoryWipeMagic.getDamageSource(player, target, data) { player?.damageSources?.create(DamageTypes.OUT_OF_WORLD, player) }
        if (target !is WitherEntity && target !is EnderDragonEntity && target !is WardenEntity) {
            target.health = .0F
            target.killed = true
            target.damage(target.damageSources.create(DamageTypes.OUT_OF_WORLD, player), Float.POSITIVE_INFINITY)
            target.onDeath(damageSource)
        } else {
            target.damage(target.maxHealth * 4.0F, damageSource)
        }
        if (!target.isAlive) {
            (target.world as? ServerWorld)?.apply {
                // repeat(10) {
                //     spawnParticles(ParticleTypes.SCULK_SOUL, target.x, target.y, target.z, 20, 0.1, .1, 0.1, 0.25)
                // }
                // repeat(128) {
                //     spawnParticles(ParticleTypes.SONIC_BOOM, target.x, target.y + it, target.z, 1, 0.0, 0.0, 0.0, 0.0)
                // }
//
                // playSound(null, target.x, target.y, target.z, SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, SoundCategory.PLAYERS, 1.0F, 1.0F, random.nextLong())
                // playSound(null, target.x, target.y, target.z, SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.PLAYERS, 1.0F, 1.0F, random.nextLong())

                target.world.server?.playerManager?.playerList?.forEach {
                    ServerPlayNetworking.send(it, ExplosionPayload(target.id))
                }
            }
        }

        if (bounces > 5 || data.isSpread || player == null || target.isAlive) {
            return
        }

        val nearestEntity = target.getNearestEntities(20.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != player && it.isAlive }
            .firstOrNull { it.getChannelQueue(player)?.isEmpty ?: true }
        if (nearestEntity != null) {
            ChannelExecutor.channel(
                SculkCatalystMagic, player, nearestEntity, SculkCatalystChannelRequest(
                    data = sculkCatalystData
                )
            )
        }
    }

    override fun getBaseCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?, data: heckerpowered.matrix.common.magic.core.ExecutionPayload): Long {
        val normalCost = when (target) {
            is EnderDragonEntity, is WitherEntity -> 110
            is PlayerEntity, is WardenEntity -> 180
            else -> getNormalCost()
        }
        val bounces = (data as? SculkCatalystExecutionPayload)?.bounces ?: 0
        return normalCost + bounces.coerceAtMost(5) * 6
    }

    override fun getCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?, data: heckerpowered.matrix.common.magic.core.ExecutionPayload, accumulator: Accumulator): Long {
        if (player.isBloodPactActive) {
            accumulator.pushCostReduction(0.5)
        }
        return super.getCost(player, target, sequence, data, accumulator)
    }

    override fun getMagicResistance(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?, data: heckerpowered.matrix.common.magic.core.ExecutionPayload): Double {
        if (target is EnderDragonEntity || target is WitherEntity || target is WardenEntity) {
            return .0
        }
        return super.getMagicResistance(player, target, sequence, data)
    }

    override fun getBaseChannelTime(player: PlayerEntity, target: LivingEntity, sequence: ChannelQueue?, data: heckerpowered.matrix.common.magic.core.ExecutionPayload): Long {
        val bounces = (data as? SculkCatalystExecutionPayload)?.bounces ?: 0
        val additionChannelTime = when (target) {
            is EnderDragonEntity, is WitherEntity -> 8 * 20L
            is WardenEntity -> 8 * 20L
            else -> 0L
        }
        return when (bounces) {
            0L -> 9 * 20
            1L -> 110 // 5.5 * 20 = 5.5s
            2L -> 3 * 20
            3L -> 30 // 30 = 1.5 * 20 = 2.5s
            4L -> 20
            5L -> 10 // 0.5s
            else -> 10 // 0.5s
        } + additionChannelTime
    }

    override fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?): MagicAvailableStatus {
        sculkCatalystTracker[player]?.removeIf {
            val sequence = player.getChannelQueue(it) ?: return@removeIf true
            return@removeIf !it.isAlive || sequence.isEmpty
        }

        val sculkCatalystIsAlreadyActive = isSculkCatalystActive(player)
        if (sculkCatalystIsAlreadyActive) {
            return MagicAvailableStatus.SCULK_CATALYST_IS_ALREADY_ACTIVE
        }
        return super.availableStatus(player, target, sequence)
    }

    fun isSculkCatalystActive(player: PlayerEntity): Boolean {
        synchronized(lock) {
            return sculkCatalystTracker[player]?.any {
                it.isAlive && (it.getChannelQueue(player)?.channelingMagics()?.firstOrNull()?.magic == SculkCatalystMagic)
            } == true
        }
    }

    override fun serializerModule(): SerializersModule =
        MagicDataSpecification<SculkCatalystExecutionPayload>().serializerModule()
}