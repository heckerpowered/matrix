/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.core.common.balance

/**
 * Executes a [CalculationPlan] against a base value and an [Accumulator].
 *
 * For each lane in plan order:
 * 1) read contributions from [Accumulator] by lane name,
 * 2) fold them into a single lane value via [Lane.operator],
 * 3) combine that lane value with running result via [Lane.combiner].
 * After all lanes, `postProcess` is applied to finalize the result.
 *
 * The [NumericCalculator] is stateless and reusable; provide a new [Accumulator] per run.
 */
class NumericCalculator(private val plan: CalculationPlan) {
    /**
     * Evaluates the final numeric result.
     *
     * @param baseValue the unmodified starting value.
     * @param accumulator contributions collected for each lane during this run.
     * @return the final computed value after all lanes and post-processing.
     */
    fun compute(baseValue: Double, accumulator: Accumulator): Double =
        plan.lanes.fold(baseValue) { currentValue, lane ->
            val contributions = accumulator.read(lane.name)
            if (contributions.isEmpty()) {
                currentValue
            } else {
                val laneValue = lane.operator.fold(contributions)
                lane.combiner(currentValue, laneValue)
            }
        }.let(plan.postProcess)
}