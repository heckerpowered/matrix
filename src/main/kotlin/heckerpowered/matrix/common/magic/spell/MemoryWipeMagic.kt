/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.asPlayerOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.rule.effect.ChannelEffect
import heckerpowered.matrix.common.magic.rule.registry.MagicRuleRegistry
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.Angerable
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.village.VillageGossipType

object MemoryWipeMagic : Magic(
    MagicDefinition(
        Matrix.identifier("memory_wipe"),
        10.mana,
        30.ticks
    )
), ChannelEffect {
    init {
        MagicRuleRegistry.register(this)
    }

    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val target = invocation.target
        val caster = invocation.caster.asPlayerOrNull()

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

        if (caster == null) {
            return
        }
        if (target is VillagerEntity) {
            val reputation = target.getReputation(caster)
            if (reputation < 0) {
                val gossips = target.gossip.entityReputationAssociatedGossips[caster.uuid]
                gossips?.set(VillageGossipType.MAJOR_NEGATIVE, 0)
                gossips?.set(VillageGossipType.MINOR_NEGATIVE, 0)
            }
        }
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

    override fun onChannel(magic: Magic, invocation: MagicInvocation) {
        val queue = invocation.queue
        val payload = invocation.payload
        if (queue.contains<MemoryWipeMagic>()) {
            payload.isSpoofed = true
        }
    }
}
