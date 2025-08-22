/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.core

class Accumulator(val initialValue: Double = 0.0) {
    fun accumulate(): Double {
        val result = (initialValue + baseBonus) * multiplier + bonus
        return result.coerceAtLeast(.0)
    }

    var baseBonus: Double = 0.0
    var bonus: Double = 0.0
    var multiplier: Double = 1.0
}