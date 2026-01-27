/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.common.balance

/**
 * Collects contributions for all lanes during a single calculation.
 *
 * Modifiers push values into an [Accumulator] by lane name.
 * Later, a [NumericCalculator] will read from the accumulator,
 * fold each lane with its [Operator], and apply the result using its combiner.
 *
 * The accumulator is ephemeral: it only lives for one calculation pass,
 * then it discarded or cleared.
 *
 * @author heckerpowered
 */
class Accumulator {
    private val lanes: MutableMap<String, MutableList<Double>> = linkedMapOf()

    /**
     * Appends a contribution to the lane identified by [laneName].
     *
     * @param laneName the name of the lane (must match a [Lane.name] in the plan).
     * @param value the numeric contribution to record.
     */
    fun push(laneName: String, value: Double) {
        val list = lanes.getOrPut(laneName) { mutableListOf() }
        list.add(value)
    }

    /**
     * Reads all contributions recorded for the lane [laneName].
     * Returns an empty list if none were recorded.
     */
    fun read(laneName: String): List<Double> = lanes[laneName].orEmpty()
}