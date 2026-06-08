/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.system

import heckerpowered.matrix.common.entity.rule.EntityUpdateContext
import heckerpowered.matrix.common.entity.rule.EntityUpdateRule
import heckerpowered.matrix.common.magic.channel.ChannelEntry
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.resolveCaster
import heckerpowered.matrix.common.magic.channel.tryResolve
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity

object ChannelQueueTicker : EntityUpdateRule {
    init {
        RuleRegistry.register<EntityUpdateRule>(this)
    }

    fun onInitialize() {}

    override fun onUpdate(context: EntityUpdateContext) {
        val entity = context.entity as? LivingEntity ?: return

        for (queue in entity.channelQueues.values) {
            val entry = queue.tick() ?: continue
            if (entity.level() !is ServerLevel) continue

            completeChannel(queue, entry)
        }
    }

    private fun completeChannel(queue: heckerpowered.matrix.common.magic.channel.ChannelQueue, entry: ChannelEntry) {
        val target = queue.target
        val caster = queue.resolveCaster()
        val invocation = MagicInvocation(caster.tryResolve(), target, queue, entry.payload)
        entry.magic.cast(invocation)
    }
}
