/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.amount

import java.math.BigDecimal

/**
 * Defines the fixed decimal scale used by ledger amounts.
 *
 * Example:
 * - decimalPlaces = 0 -> whole units
 * - decimalPlaces = 2 -> cents-like precision
 * - decimalPlaces = 3 -> milli-units
 */
data class AmountScale(
    val decimalPlaces: Int,
) {
    val scaleFactor = checkedPowerOfTen(decimalPlaces)

    /**
     * Creates an [Amount] from whole units.
     *
     * Example:
     * - scale 2, wholeUnits 12 -> 12.00
     */
    fun fromWholeUnits(wholeUnits: Long): Amount {
        require(wholeUnits >= 0L) { "wholeUnits must be non-negative." }

        val scaledUnits = Math.multiplyExact(wholeUnits, scaleFactor)
        return Amount.fromScaledUnits(scaledUnits)
    }

    fun toDouble(scaledUnits: Long): Double {
        return scaledUnits.toDouble() / scaleFactor.toDouble()
    }

    fun toBigDecimal(scaledUnits: Long): BigDecimal {
        return BigDecimal(scaledUnits).movePointLeft(decimalPlaces)
    }

    companion object {
        val Whole = of(0)
        val Cents = of(2)
        val Milli = of(3)

        fun of(decimalPlaces: Int): AmountScale {
            return AmountScale(decimalPlaces)
        }

        private fun checkedPowerOfTen(exponent: Int): Long {
            if (exponent < 0) {
                throw ArithmeticException("Negative exponent: $exponent")
            }

            var result = 1L
            repeat(exponent) {
                result = Math.multiplyExact(result, 10L)
            }
            return result
        }
    }
}
