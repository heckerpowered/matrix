/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.core.common.balance

/**
 * Defines how a group of numeric contributions (values pushed by modifiers)
 * should be aggregated into a single number.
 *
 * An operator provides the folding rule for all values in a lane.
 * For example, values may be multiplied, summed, or clamped.
 *
 * @author heckerpowered
 */
fun interface Operator {
    /**
     * Aggregates all contributed values into a single number.
     *
     * @param values the list of values contributed to a lane (maybe empty).
     * @return the fold result for this lane.
     */
    fun fold(values: List<Double>): Double
}