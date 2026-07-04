/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.system

import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.CalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.CalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.MaxManaCalculationSink
import heckerpowered.matrix.common.persistent.manaClock
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.server.level.ServerPlayer

/**
 * Applies the player's mana overclock rate (HUD M key, persisted in
 * [heckerpowered.matrix.common.persistent.OverclockState]) to max-mana calculation.
 *
 * Pre-migration behavior contract: the overclock handler wrote `maxMana += delta * 100` on a
 * 100-base persistent pool, i.e. `maxMana = base * rate`. Max mana is a derived value in the
 * ledger system (recomputed from the worn helmet every tick), so the same observable scaling
 * is contributed here as a multiplier instead of a one-shot write; mana above the shrunk
 * bound is clamped by the account's transaction constraints exactly like the old handler's
 * explicit clamp, and the regeneration ticker syncs the result to the HUD.
 */
object ManaOverclockRule : CalculationContributor {
    fun onInitialize() {
        RuleRegistry.register<CalculationContributor>(this)
    }

    override fun contribute(context: MagicCalculationContext, sink: CalculationSink) {
        if (sink !is MaxManaCalculationSink) return
        val player = context.playerOrNull() as? ServerPlayer ?: return
        sink.multiplier *= player.manaClock
    }
}
