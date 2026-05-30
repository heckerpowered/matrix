/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

fun interface EffectRemovalRule {
    fun onRemoval(context: EffectRemovalContext)
}