/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.data.language.MatrixLanguage
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

object MemoryEraseMagic : Magic(MatrixLanguage.magicMemoryErase, 10, MatrixLanguage.magicMemoryEraseDescription, 30) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
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
    fun getDamageSource(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, supplier: () -> DamageSource?): DamageSource {
        contract {
            callsInPlace(supplier, InvocationKind.AT_MOST_ONCE)
        }

        val erasedSource = target.world.damageSources.create(MatrixDamageTypes.magic, player)
        if (player == null || sequence.sequencedAfter<MemoryEraseMagic>()) {
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
