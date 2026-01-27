/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.common.balance

/**
 * Sum all contributions.
 *
 * @author heckerpowered
 */
object SumOperator : Operator {
    override fun fold(values: List<Double>) = values.sum()
}