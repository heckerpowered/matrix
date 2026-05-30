/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.effect

import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.all

object MagicCastPipeline {
    fun onCast(magic: Magic, invocation: MagicInvocation) {
        RuleRegistry.all<CastEffect>()
            .forEach { it.onCast(magic, invocation) }
    }
}