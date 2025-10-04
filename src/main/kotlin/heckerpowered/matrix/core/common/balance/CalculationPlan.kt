/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.core.common.balance

/**
 * Declarative description of how to evaluate a numeric result.
 *
 * A [CalculationPlan] specifies:
 * - The ordered list of [Lane]s to be applied.
 * - For each lane, how contributions are folded and combined.
 * - An optional [postProcess] step to finalize the result (e.g. clamp non-negative, rounding).
 *
 * It does not perform any calculation itself; it is a blueprint
 * consumed by [NumericCalculator].
 *
 * Example:
 * ```
 * val plan = CalculationPlan(
 *     lanes = listOf(
 *          Lane("multi.core", MultiplyOperator, { current, lane -> current * lane }),
 *          Lane("add.flat", SumOperator, { current, lane -> current + lane })
 *     ),
 *     postProcess = { result -> result.coerceAtLeast(.0) }
 * )
 * ```
 *
 * @author heckerpowered
 */
data class CalculationPlan(
    val lanes: List<Lane>,
    val postProcess: (Double) -> Double = { it },
)