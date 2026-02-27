/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.event.ReadDataCallback
import heckerpowered.matrix.common.event.WriteDataCallback
import heckerpowered.matrix.common.magic.channel.*
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.CostCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.MagicCalculationSink
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.common.network.ExplosionPayload
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.extension.EntityExtension.damage
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
import net.minecraft.server.world.ServerWorld

object SculkCatalystMagic : Magic(
    MagicDefinition(
        Matrix.identifier("sculk_catalyst"),
        12.mana,
        (9 * 20).ticks
    )
), MagicExecutionPayloadSpecification, MagicCalculationContributor {

    @Serializable
    private class SculkCatalystExecutionPayload(
        var bounces: Long = 0,
    ) : ExecutionPayload()

    private class SculkCatalystChannelAttempt(
        bypassLock: Boolean = false,
        costMana: Boolean = true,
    ) : ChannelAttempt(bypassLock, costMana) {
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
        RuleRegistry.register<MagicCalculationContributor>(this)
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

    override fun channel(invocation: MagicInvocation) {
        super.channel(invocation)
        val caster = invocation.caster.entityOrNull() as? PlayerEntity ?: return
        val target = invocation.target
        synchronized(lock) {
            sculkCatalystTracker.computeIfAbsent(caster) {
                mutableListOf()
            }.add(target)
        }
    }

    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.asPlayerOrNull()
        val target = invocation.target
        val payload = invocation.payload

        val spreadPayload = payload as? SculkCatalystExecutionPayload ?: SculkCatalystExecutionPayload()
        spreadPayload.inheritFrom(payload)
        val bounces = ++spreadPayload.bounces

        val damageSource = invocation.removeSourceIfSpoofed { caster?.damageSources?.create(DamageTypes.OUT_OF_WORLD, caster) }
        if (target !is WitherEntity && target !is EnderDragonEntity && target !is WardenEntity) {
            target.health = .0F
            target.killed = true
            target.damage(target.damageSources.create(DamageTypes.OUT_OF_WORLD, caster), Float.POSITIVE_INFINITY)
            target.onDeath(damageSource)
        } else {
            target.damage(target.maxHealth * 4.0F, damageSource)
        }
        if (!target.isAlive) {
            (target.world as? ServerWorld)?.apply {
                target.world.server?.playerManager?.playerList?.forEach {
                    ServerPlayNetworking.send(it, ExplosionPayload(target.id))
                }
            }
        }

        if (bounces > 5 || payload.isSpread || caster == null || target.isAlive) {
            return
        }

        val nearestEntity = target.getNearestEntities(20.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != caster && it.isAlive }
            .firstOrNull { it.getChannelQueue(caster)?.isEmpty ?: true }
        if (nearestEntity != null) {
            val spreadInvocation = MagicInvocation.fromEntity(caster, nearestEntity, spreadPayload)
            val spreadAttempt = SculkCatalystChannelAttempt()
            ChannelExecutor.channel(SculkCatalystMagic, spreadInvocation, spreadAttempt)
        }
    }

    override fun getBaseCost(context: MagicCalculationContext): Long {
        val target = context.target
        val payload = context.payload
        val normalCost = when (context.targetRank()) {
            SpellRank.BOSS -> 110
            SpellRank.CHIMERA -> 180
            else -> if (target is PlayerEntity) 180 else getNormalCost()
        }
        val bounces = (payload as? SculkCatalystExecutionPayload)?.bounces ?: 0
        return normalCost + bounces.coerceAtMost(5) * 6
    }

    override fun getMagicResistance(context: MagicCalculationContext): Double {
        val target = context.target
        if (target is EnderDragonEntity || target is WitherEntity || target is WardenEntity) {
            return .0
        }
        return super.getMagicResistance(context)
    }

    override fun getBaseChannelTime(context: MagicCalculationContext): Long {
        val payload = context.payload

        val bounces = (payload as? SculkCatalystExecutionPayload)?.bounces ?: 0
        val additionChannelTime = when (context.targetRank()) {
            SpellRank.BOSS -> 8 * 20L
            SpellRank.CHIMERA -> 8 * 20L
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

    override fun availableStatus(context: MagicCalculationContext): MagicAvailableStatus {
        val caster = context.playerOrNull() ?: return super.availableStatus(context)
        sculkCatalystTracker[caster]?.removeIf {
            val sequence = caster.getChannelQueue(it) ?: return@removeIf true
            return@removeIf !it.isAlive || sequence.isEmpty
        }

        val sculkCatalystIsAlreadyActive = isSculkCatalystActive(caster)
        if (sculkCatalystIsAlreadyActive) {
            return MagicAvailableStatus.SCULK_CATALYST_IS_ALREADY_ACTIVE
        }
        return super.availableStatus(context)
    }

    fun isSculkCatalystActive(player: PlayerEntity): Boolean {
        synchronized(lock) {
            return sculkCatalystTracker[player]?.any {
                it.isAlive && (it.getChannelQueue(player)?.channelingMagics()?.firstOrNull()?.magic == SculkCatalystMagic)
            } == true
        }
    }

    override fun serializerModule(): SerializersModule =
        MagicExecutionPayloadSpecification<SculkCatalystExecutionPayload>().serializerModule()

    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        if (sink !is CostCalculationSink) return
        if (magic !is SculkCatalystMagic) return
        val caster = context.playerOrNull() ?: return
        if (!caster.isBloodPactActive) return

        sink.costReduction += 0.5
    }
}