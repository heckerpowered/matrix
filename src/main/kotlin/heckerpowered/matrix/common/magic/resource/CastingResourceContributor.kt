/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.resource

import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.registry.MagicRuleContributor

/**
 * Declares a source of casting resources for magic cost evaluation.
 *
 * A contributor inspects a [MagicCalculationContext] and conditionally
 * provides one or more [CastingResource] instances that may participate
 * in cost calculation and consumption.
 *
 * Implementations must be:
 * - Pure (no state mutation).
 * - Deterministic for the same context.
 * - Free of side effects.
 *
 * Contributors are evaluated during the resource collection phase and
 * must not perform any actual resource consumption.
 */
fun interface CastingResourceContributor : MagicRuleContributor {
    /**
     * Contributes available casting resources into the given sink.
     *
     * Implementations should inspect the provided [context] and, if their
     * activation conditions are met, add corresponding [CastingResource]
     * instances into [sink].
     *
     * The provided [sink]:
     * - Is scoped to a single evaluation.
     * - Must not be stored or retained.
     * - May already contain other resources.
     *
     * This method must not:
     * - Consume resources.
     * - Mutate game state.
     * - Depend on external mutable state.
     *
     * @param magic the magic instance currently being evaluated; used to
     *              determine whether this contributor applies to the given
     *              magic.
     * @param context calculation context describing the current casting scenario.
     * @param sink collection to which available [CastingResource] instances
     *             should be added.
     */
    fun contribute(magic: Magic, context: MagicCalculationContext, sink: MutableCollection<CastingResource>)
}