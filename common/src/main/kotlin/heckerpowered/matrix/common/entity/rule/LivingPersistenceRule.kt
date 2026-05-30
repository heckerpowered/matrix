/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

interface LivingPersistenceRule {
    fun save(context: LivingSaveContext) {}
    fun load(context: LivingLoadContext) {}
}