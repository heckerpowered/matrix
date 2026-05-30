/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.effect

import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.Magic

fun interface CastEffect : MagicEffectRuleContributor {
    fun onCast(magic: Magic, invocation: MagicInvocation)
}