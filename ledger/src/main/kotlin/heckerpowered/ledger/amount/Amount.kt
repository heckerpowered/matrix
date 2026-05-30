/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.amount

@JvmInline
value class Amount private constructor(val scaledUnits: Long) : Comparable<Amount> {
    init {
        require(scaledUnits >= 0) { "scaledUnits must be non-negative" }
    }

    operator fun plus(other: Amount): Amount {
        return fromScaledUnits(
            Math.addExact(scaledUnits, other.scaledUnits)
        )
    }

    operator fun minus(other: Amount): Amount {
        val result = Math.subtractExact(scaledUnits, other.scaledUnits)
        require(result >= 0L) { "Resulting amount must be non-negative." }
        return fromScaledUnits(result)
    }

    fun isZero(): Boolean {
        return scaledUnits == 0L
    }

    override fun compareTo(other: Amount): Int {
        return scaledUnits.compareTo(other.scaledUnits)
    }

    companion object {
        val Zero = Amount(0L)

        fun fromScaledUnits(scaledUnits: Long): Amount {
            return Amount(scaledUnits)
        }
    }
}