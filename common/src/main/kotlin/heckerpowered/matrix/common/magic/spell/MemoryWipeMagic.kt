/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.asPlayerOrNull
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.rule.effect.ChannelEffect
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.NeutralMob
import net.minecraft.world.entity.ai.gossip.GossipType
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.npc.villager.Villager

object MemoryWipeMagic : Magic(
    MagicDefinition(
        Matrix.identifier("memory_wipe"),
        8.mana,
        30.ticks
    )
), ChannelEffect {
    init {
        RuleRegistry.register<ChannelEffect>(this)
    }

    private val MEMORY_WIPE_PRESERVED_MEMORIES = setOf(
        // Long-term identity / life data
        MemoryModuleType.HOME,
        MemoryModuleType.JOB_SITE,
        MemoryModuleType.POTENTIAL_JOB_SITE,
        MemoryModuleType.MEETING_POINT,
        MemoryModuleType.SECONDARY_JOB_SITE,
        MemoryModuleType.LAST_SLEPT,
        MemoryModuleType.LAST_WOKEN,
        MemoryModuleType.LAST_WORKED_AT_POI,
        MemoryModuleType.HIDING_PLACE,

        // Long-term visited / explored / unreachable records
        MemoryModuleType.VISITED_BLOCK_POSITIONS,
        MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS,
        MemoryModuleType.SNIFFER_EXPLORED_POSITIONS,

        // Allay / social long-term bindings
        MemoryModuleType.LIKED_PLAYER,
        MemoryModuleType.LIKED_NOTEBLOCK_POSITION,

        // Cooldowns / timing / balance state
        MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
        MemoryModuleType.GAZE_COOLDOWN_TICKS,
        MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
        MemoryModuleType.HAS_HUNTING_COOLDOWN,
        MemoryModuleType.RAM_COOLDOWN_TICKS,
        MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS,
        MemoryModuleType.CHARGE_COOLDOWN_TICKS,
        MemoryModuleType.ATTACK_TARGET_COOLDOWN,
        MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
        MemoryModuleType.ATTACK_COOLING_DOWN,
        MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS,

        // Warden / vibration / sonic boom cooldowns
        MemoryModuleType.DIG_COOLDOWN,
        MemoryModuleType.ROAR_SOUND_COOLDOWN,
        MemoryModuleType.SNIFF_COOLDOWN,
        MemoryModuleType.TOUCH_COOLDOWN,
        MemoryModuleType.VIBRATION_COOLDOWN,
        MemoryModuleType.SONIC_BOOM_COOLDOWN,
        MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN,

        // Breeze cooldowns / rhythm gates
        MemoryModuleType.BREEZE_JUMP_COOLDOWN,
        MemoryModuleType.BREEZE_SHOOT_COOLDOWN,
    )

    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val target = invocation.target
        val caster = invocation.caster.asPlayerOrNull()

        target.brain.memories
            .filter { it.key !in MEMORY_WIPE_PRESERVED_MEMORIES }
            .forEach { it.value.clear() }
        if (target is Mob) {
            target.target = null
            target.targetSelector.availableGoals
                .map { it.goal }
                .forEach { it.stop() }
        }

        if (target is NeutralMob) {
            target.stopBeingAngry()
        }
        target.lastHurtByMob = null
        target.lastHurtByPlayer = null
        target.lastHurtByPlayerMemoryTime = 0

        if (caster == null) return
        if (target is Villager) {
            target.gossips.remove(caster.uuid, GossipType.MAJOR_NEGATIVE)
            target.gossips.remove(caster.uuid, GossipType.MINOR_NEGATIVE)
        }
    }

    @JvmStatic
    fun LivingEntity.clearTargetingEntity() {
        brain.clearMemories()
        if (this is Mob) {
            target = null
            targetSelector.availableGoals
                .map { it.goal }
                .forEach { it.stop() }
        }
        if (this is NeutralMob) {
            stopBeingAngry()
        }
        lastHurtByPlayer = null
        lastHurtByPlayerMemoryTime = 0
        lastHurtByMob = null
    }

    override fun onChannel(magic: Magic, invocation: MagicInvocation) {
        val queue = invocation.queue
        val payload = invocation.payload
        if (queue.contains<MemoryWipeMagic>()) {
            payload.isSpoofed = true
        }
    }

    override fun getBaseCost(context: MagicCalculationContext): Long {
        if (context.targetRank() == SpellRank.BOSS) {
            return 25
        }
        return super.getBaseCost(context)
    }
}
