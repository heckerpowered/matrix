/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.resource

import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.CastingResource
import heckerpowered.matrix.common.magic.resource.CastingResourceContributor
import heckerpowered.matrix.common.magic.resource.CastingResourceSet
import heckerpowered.matrix.common.magic.resource.ManaResourceContributor
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.all
import heckerpowered.matrix.common.rule.register

object CastingResourcePipeline {
    init {
        RuleRegistry.register<CastingResourceContributor>(ManaResourceContributor)
    }

    fun collect(context: MagicCalculationContext): CastingResourceSet {
        val collected = mutableListOf<CastingResource>()

        RuleRegistry.all<CastingResourceContributor>()
            .forEach { it.contribute(context, collected) }

        val normalized = collected
            .asSequence()
            .distinctBy { it::class }
            .sortedByDescending { it.priority }
            .toList()
        return CastingResourceSet(normalized)
    }
}