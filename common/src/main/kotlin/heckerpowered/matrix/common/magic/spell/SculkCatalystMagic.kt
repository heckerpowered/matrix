/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.entity.EntityPolarity
import heckerpowered.matrix.common.magic.channel.*
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.CostCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.MagicCalculationSink
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.common.network.ClientboundExplosionPayload
import heckerpowered.matrix.common.persistent.QueueLoadedContext
import heckerpowered.matrix.common.persistent.QueueLoadedRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.extension.damage
import heckerpowered.matrix.core.utility.getNearestEntities
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.monster.warden.Warden
import net.minecraft.world.entity.player.Player
import java.util.*

object SculkCatalystMagic : Magic(
    MagicDefinition(
        Matrix.identifier("sculk_catalyst"),
        12.mana,
        (9 * 20).ticks
    )
), MagicExecutionPayloadSpecification, MagicCalculationContributor, QueueLoadedRule {

    @Serializable
    private class SculkCatalystExecutionPayload(
        var bounces: Long = 0,
    ) : ExecutionPayload()

    private class SculkCatalystChannelAttempt(
        bypassLock: Boolean = false,
        costMana: Boolean = true,
    ) : ExecutionPayload(bypassLock, costMana) {
        override fun isMagicAvailable(availableStatus: LMagicAvailableStatus): Boolean {
            if (availableStatus == LMagicAvailableStatus.SCULK_CATALYST_IS_ALREADY_ACTIVE) {
                return true
            }
            return super.isMagicAvailable(availableStatus)
        }
    }

    init {
        RuleRegistry.register<MagicCalculationContributor>(this)
        RuleRegistry.register<QueueLoadedRule>(this)
        ServerTickEvents.START_SERVER_TICK.register(::onServerTick)
    }

    private fun onServerTick(minecraftServer: MinecraftServer) {
        if (minecraftServer.tickCount % 20 != 0) return

        val iterator = sculkCatalystTracker.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.removeIf { !it.isAlive || it.channelQueues.isEmpty() }
            if (entry.value.isEmpty()) {
                iterator.remove()
            }
        }
    }

    private val sculkCatalystTracker = mutableMapOf<UUID, MutableList<LivingEntity>>()

    override fun channel(invocation: MagicInvocation) {
        super.channel(invocation)
        val caster = invocation.caster.entityOrNull() as? Player ?: return
        val target = invocation.target

        sculkCatalystTracker.computeIfAbsent(caster.uuid) { mutableListOf() }
            .add(target)
    }

    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.asPlayerOrNull()
        val target = invocation.target
        val level = target.level() as? ServerLevel ?: return
        val payload = invocation.payload

        val spreadPayload = payload.specialize(::SculkCatalystExecutionPayload)
        val bounces = ++spreadPayload.bounces

        val damageSource = invocation.defaultDamageSource(DamageTypes.GENERIC_KILL)
        if (target !is WitherBoss && target !is EnderDragon && target !is Warden) {
            target.health = .0F
            target.polarity = target.polarity or EntityPolarity.ZERO_HEALTH_SPOOF or EntityPolarity.FORCE_DEATH_CHECK
            target.hurtServer(level, damageSource, Float.MAX_VALUE)
            target.die(damageSource)
        } else {
            target.damage(target.maxHealth * 4.0F, damageSource)
        }
        if (target.isDeadOrDying) {
            target.level().server?.playerList?.players?.forEach {
                ServerPlayNetworking.send(it, ClientboundExplosionPayload(target.id))
            }
        }

        // A Sculk Catalyst cast created by the generic spread mechanism must not start
        // another Sculk Catalyst bounce chain. Only bounces created by Sculk Catalyst
        // itself may continue, up to the bounce limit.
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

    override fun onLoaded(context: QueueLoadedContext) {
        if (context.queue.contains<SculkCatalystMagic>()) {
            sculkCatalystTracker.computeIfAbsent(context.uuid) { mutableListOf() }
                .add(context.entity)
        }
    }

    override fun getBaseCost(context: MagicCalculationContext): Long {
        val target = context.target
        val payload = context.payload
        val normalCost = when (context.targetRank()) {
            SpellRank.BOSS -> 110
            SpellRank.CHIMERA -> 180
            else -> if (target is Player) 180 else getNormalCost()
        }
        val bounces = (payload as? SculkCatalystExecutionPayload)?.bounces ?: 0
        return normalCost + bounces.coerceAtMost(5) * 6
    }

    override fun getMagicResistance(context: MagicCalculationContext): Double {
        val target = context.target
        if (target is EnderDragon || target is WitherBoss || target is Warden) {
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

    override fun availableStatus(context: MagicCalculationContext): LMagicAvailableStatus {
        val caster = context.playerOrNull() ?: return super.availableStatus(context)
        if (isSculkCatalystActive(caster)) return LMagicAvailableStatus.SCULK_CATALYST_IS_ALREADY_ACTIVE
        return super.availableStatus(context)
    }

    fun isSculkCatalystActive(player: Player): Boolean {
        val trackedEntities = sculkCatalystTracker[player.uuid] ?: return false
        return trackedEntities.asSequence()
            .filter { it.isAlive }
            .mapNotNull { it.getChannelQueue(player) }
            .mapNotNull { it.active }
            .any { it.magic == SculkCatalystMagic }
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