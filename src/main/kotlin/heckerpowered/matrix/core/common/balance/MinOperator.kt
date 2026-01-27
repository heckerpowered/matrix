/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.common.balance


/**
 * Returns the minimum contribution.
 * Identity for empty input is 0.0.
 *
 * @author heckerpowered
 */
object MinOperator : Operator {
    override fun fold(values: List<Double>) = values.minOrNull() ?: 0.0
}