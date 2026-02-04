/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.calculation.pipeline

import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.MagicCalculationSink
import heckerpowered.matrix.common.magic.rule.registry.MagicRuleRegistry

object MagicCalculationPipeline {
    fun apply(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        MagicRuleRegistry.all()
            .asSequence()
            .filterIsInstance<MagicCalculationContributor>()
            .forEach { it.contribute(magic, context, sink) }
    }
}