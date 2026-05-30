/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.ability

data class HealMeasurement(
    val resolvedAmount: Float,
    val restoredHealth: Float,
) {
    val overflowAmount: Float
        get() = (resolvedAmount - restoredHealth).coerceAtLeast(0.0F)
}