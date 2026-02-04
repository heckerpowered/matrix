/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.effect

import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.rule.registry.MagicRuleRegistry

object MagicChannelPipeline {
    fun onChannel(magic: Magic, invocation: MagicInvocation) {
        MagicRuleRegistry.all()
            .asSequence()
            .filterIsInstance<ChannelEffect>()
            .forEach { it.onChannel(magic, invocation) }
    }
}