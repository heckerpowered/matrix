/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.resource

import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext

object ManaResourceContributor : CastingResourceContributor {
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
     * @param context calculation context describing the current casting scenario.
     * @param sink collection to which available [CastingResource] instances
     *             should be added.
     */
    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MutableCollection<CastingResource>) {
        sink += ManaReserve()
    }
}