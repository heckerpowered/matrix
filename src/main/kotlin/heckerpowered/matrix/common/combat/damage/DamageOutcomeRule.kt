/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

fun interface DamageOutcomeRule {
    fun onOutcome(context: DamageOutcomeContext)
}