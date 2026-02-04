/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.rule.registry

object MagicRuleRegistry {
    private val contributors = mutableListOf<MagicRuleContributor>()

    fun register(contributor: MagicRuleContributor) {
        contributors += contributor
    }

    fun all(): List<MagicRuleContributor> = contributors
}