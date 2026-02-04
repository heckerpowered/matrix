/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.calculation.contributor

import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.sink.CalculationSink
import heckerpowered.matrix.common.magic.rule.registry.MagicRuleContributor

fun interface CalculationContributor : MagicRuleContributor {
    fun contribute(context: MagicCalculationContext, sink: CalculationSink)
}