/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.resource

import heckerpowered.matrix.common.magic.core.MagicCalculationContext

/**
 * Central registry responsible for collecting casting resources
 * contributed by various systems.
 *
 * The registry maintains a set of [CastingResourceContributor]s and
 * performs resource collection for a given [MagicCalculationContext].
 *
 * This type is intentionally:
 * - Stateless with respect to individual calculations.
 * - Order-independent for contributor registration.
 * - Deterministic for the same context and contributor set.
 *
 * The registry does not:
 * - Perform resource consumption.
 * - Evaluate affordability.
 * - Apply prioritization or allocation strategies.
 *
 * Those responsibilities belong to higher-level orchestration logic.
 */
object CastingResourceRegistry {
    private val contributors = mutableListOf<CastingResourceContributor>()

    init {
        register(ManaResourceContributor)
    }

    /**
     * Registers a new [CastingResourceContributor].
     *
     * Contributors are expected to be long-lived and are typically
     * registered during mod initialization.
     *
     * Registration order does not affect the final collection result.
     *
     * @param contributor the contributor to register.
     */
    fun register(contributor: CastingResourceContributor) {
        contributors += contributor
    }

    /**
     * Collects all available [CastingResource]s for the given context.
     *
     * Each registered [CastingResourceContributor] is invoked exactly once.
     * The resulting resources are de-duplicated by their concrete class,
     * ensuring that at most one instance of each resource type is returned.
     *
     * If multiple contributors provide the same resource type, the first
     * encountered instance is retained and subsequent ones are ignored.
     *
     * This method:
     * - Does not mutate the context.
     * - Does not consume any resources.
     * - Allocates a fresh collection per invocation.
     *
     * @param context calculation context describing the current casting scenario.
     * @return a list of unique [CastingResource] instances available under
     *         the given context.
     */
    fun collect(context: MagicCalculationContext): CastingResourceSet {
        val collected = mutableListOf<CastingResource>()
        for (contributor in contributors) {
            contributor.contribute(context, collected)
        }
        val normalized = collected
            .distinctBy { it::class }
            .sortedByDescending { it.priority }
        return CastingResourceSet(normalized)
    }
}