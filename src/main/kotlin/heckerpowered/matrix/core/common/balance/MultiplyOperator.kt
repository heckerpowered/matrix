/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.core.common.balance

/**
 * Multiplies all values together.
 *
 * @author heckerpowered
 */
object MultiplyOperator : Operator {
    override fun fold(values: List<Double>) = values.fold(1.0) { product, contribution ->
        product * contribution
    }
}