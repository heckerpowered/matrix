/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.calculation.pipeline

import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.CalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.CalculationSink
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.all

object CalculationPipeline {
    fun apply(context: MagicCalculationContext, sink: CalculationSink) {
        RuleRegistry.all<CalculationContributor>()
            .forEach { it.contribute(context, sink) }
    }
}