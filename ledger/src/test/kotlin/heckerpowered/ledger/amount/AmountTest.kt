/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.amount/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

import kotlin.test.*

/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

class AmountTest {
    @Test
    fun zeroShouldBeZero() {
        assertEquals(0L, Amount.Zero.scaledUnits)
        assertTrue(Amount.Zero.isZero())
    }

    @Test
    fun fromScaledUnitsShouldCreateAmount() {
        val amount = Amount.fromScaledUnits(1234)

        assertEquals(1234L, amount.scaledUnits)
    }

    @Test
    fun fromScaledUnitsShouldRejectNegativeInput() {
        assertFailsWith<IllegalArgumentException> {
            Amount.fromScaledUnits(-1)
        }
    }

    @Test
    fun plusShouldAddTwoAmounts() {
        val left = Amount.fromScaledUnits(120)
        val right = Amount.fromScaledUnits(30)

        val result = left + right

        assertEquals(150L, result.scaledUnits)
    }

    @Test
    fun plusShouldKeepZeroSemantics() {
        val amount = Amount.fromScaledUnits(42)

        assertEquals(amount, amount + Amount.Zero)
        assertEquals(amount, Amount.Zero + amount)
    }

    @Test
    fun plusShouldFailOnOverflow() {
        val left = Amount.fromScaledUnits(Long.MAX_VALUE)
        val right = Amount.fromScaledUnits(1)

        assertFailsWith<ArithmeticException> {
            left + right
        }
    }

    @Test
    fun minusShouldSubtractTwoAmounts() {
        val left = Amount.fromScaledUnits(120)
        val right = Amount.fromScaledUnits(30)

        val result = left - right

        assertEquals(90L, result.scaledUnits)
    }

    @Test
    fun minusShouldSupportExactZero() {
        val left = Amount.fromScaledUnits(30)
        val right = Amount.fromScaledUnits(30)

        val result = left - right

        assertEquals(0L, result.scaledUnits)
        assertTrue(result.isZero())
    }

    @Test
    fun minusShouldRejectNegativeResult() {
        val left = Amount.fromScaledUnits(10)
        val right = Amount.fromScaledUnits(11)

        assertFailsWith<IllegalArgumentException> {
            left - right
        }
    }

    @Test
    fun compareToShouldCompareByScaledUnits() {
        val small = Amount.fromScaledUnits(10)
        val large = Amount.fromScaledUnits(20)

        assertTrue(small < large)
        assertTrue(large > small)
        assertEquals(0, small.compareTo(Amount.fromScaledUnits(10)))
    }

    @Test
    fun isZeroShouldDistinguishZeroAndNonZero() {
        assertTrue(Amount.Zero.isZero())
        assertFalse(Amount.fromScaledUnits(1).isZero())
    }
}