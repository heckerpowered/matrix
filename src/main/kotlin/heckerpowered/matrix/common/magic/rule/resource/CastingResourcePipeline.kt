/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.resource

import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.CastingResource
import heckerpowered.matrix.common.magic.resource.CastingResourceContributor
import heckerpowered.matrix.common.magic.resource.CastingResourceSet
import heckerpowered.matrix.common.magic.resource.ManaResourceContributor
import heckerpowered.matrix.common.magic.rule.registry.MagicRuleRegistry

object CastingResourcePipeline {
    init {
        MagicRuleRegistry.register(ManaResourceContributor)
    }

    fun collect(magic: Magic, context: MagicCalculationContext): CastingResourceSet {
        val collected = mutableListOf<CastingResource>()

        MagicRuleRegistry.all()
            .asSequence()
            .filterIsInstance<CastingResourceContributor>()
            .forEach { it.contribute(magic, context, collected) }

        val normalized = collected
            .asSequence()
            .distinctBy { it::class }
            .sortedByDescending { it.priority }
            .toList()
        return CastingResourceSet(normalized)
    }
}