/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.common.balance

/**
 * Returns the maximum contribution.
 * Identity for empty input is 0.0.
 *
 * @author heckerpowered
 */
object MaxOperator : Operator {
    override fun fold(values: List<Double>) = values.maxOrNull() ?: 0.0
}