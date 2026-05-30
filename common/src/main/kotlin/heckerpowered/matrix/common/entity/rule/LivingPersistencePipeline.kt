/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.all

object LivingPersistencePipeline {
    @JvmStatic
    fun save(context: LivingSaveContext) {
        for (rule in RuleRegistry.all<LivingPersistenceRule>()) {
            rule.save(context)
        }
    }

    @JvmStatic
    fun load(context: LivingLoadContext) {
        for (rule in RuleRegistry.all<LivingPersistenceRule>()) {
            rule.load(context)
        }
    }
}