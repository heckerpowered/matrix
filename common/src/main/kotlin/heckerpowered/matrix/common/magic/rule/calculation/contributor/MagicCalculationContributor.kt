/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.calculation.contributor

import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.sink.MagicCalculationSink
import heckerpowered.matrix.common.magic.rule.registry.MagicRuleContributor

/**
 * Contributes calculation modifiers into a calculation sink.
 */
fun interface MagicCalculationContributor : MagicRuleContributor {
    fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink)
}