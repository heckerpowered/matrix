/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.common.balance

/**
 * A named line in a calculation plane.
 *
 * A lane groups together all contributions addressed to [name].
 * At evaluation time:
 * 1. All contributions are folded into a single number by [operator].
 * 2. The result number is combined with running result using [combiner].
 *
 * Example lanes:
 * - "mul.core" with a multiply operator, combined by multiplication.
 * - "add.flat" with a sum operator, combined by addition.
 * - "cap.min" with a max operator, combined by taking `max(current, laneValue)`.
 *
 * @property name Identifier of the lane. Modifiers push contributions to this name.
 * @property operator Defines how contributions in this lane are folded into one value.
 * @property combiner Function describing how the folded lane value is merged with the
 * accumulated result so far.
 * - `currentValue`: the running result up to this lane (starts from baseValue).
 * - `laneValue`: the folded result of this lane's contributions.
 *
 * @author heckerpowered
 */
data class Lane(
    val name: String,
    val operator: Operator,
    val combiner: (currentValue: Double, laneValue: Double) -> Double,
)