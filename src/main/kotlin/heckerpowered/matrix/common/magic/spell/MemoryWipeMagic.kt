/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.ChannelQueue
import heckerpowered.matrix.common.magic.ExecutionPayload
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Magic
import heckerpowered.matrix.common.magic.MagicDefinition
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.Angerable
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.village.VillageGossipType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object MemoryWipeMagic : Magic(
    MagicDefinition(
        Matrix.identifier("memory_wipe"),
        10.mana,
        30.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: ExecutionPayload) {
        super.cast(player, target, sequence, data)
        target.brain.clear()
        if (target is MobEntity) {
            target.target = null
            target.targetSelector.goals
                .map { it.goal }
                .forEach { it.stop() }
        }
        if (target is Angerable) {
            target.stopAnger()
        }
        target.attacker = null

        if (player == null) {
            return
        }
        if (target is VillagerEntity) {
            val reputation = target.getReputation(player)
            if (reputation < 0) {
                val gossips = target.gossip.entityReputationAssociatedGossips[player.uuid]
                gossips?.set(VillageGossipType.MAJOR_NEGATIVE, 0)
                gossips?.set(VillageGossipType.MINOR_NEGATIVE, 0)
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    fun getDamageSource(player: ServerPlayerEntity?, target: LivingEntity, data: ExecutionPayload, supplier: () -> DamageSource?): DamageSource {
        contract {
            callsInPlace(supplier, InvocationKind.AT_MOST_ONCE)
        }

        val erasedSource = target.world.damageSources.create(MatrixDamageTypes.magic)
        if (player == null || data.isSpoofed) {
            return erasedSource
        }

        val result = supplier() ?: return erasedSource
        return result
    }

    @JvmStatic
    fun LivingEntity.clearTargetingEntity() {
        brain.clear()
        if (this is MobEntity) {
            target = null
            targetSelector.goals
                .map { it.goal }
                .forEach { it.stop() }
        }
        if (this is Angerable) {
            stopAnger()
        }
        attacker = null
    }

}
