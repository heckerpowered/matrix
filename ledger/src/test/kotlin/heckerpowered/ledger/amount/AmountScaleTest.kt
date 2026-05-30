/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.amount/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AmountScaleTest {
    @Test
    fun wholeScaleShouldUseScaleFactor1() {
        val scale = AmountScale.Whole

        assertEquals(0, scale.decimalPlaces)
        assertEquals(1L, scale.scaleFactor)
    }

    @Test
    fun centsScaleShouldUseScaleFactor100() {
        val scale = AmountScale.Cents

        assertEquals(2, scale.decimalPlaces)
        assertEquals(100L, scale.scaleFactor)
    }

    @Test
    fun milliScaleShouldUseScaleFactor1000() {
        val scale = AmountScale.Milli

        assertEquals(3, scale.decimalPlaces)
        assertEquals(1000L, scale.scaleFactor)
    }

    @Test
    fun ofShouldCreateScaleWithCorrectFactor() {
        val scale = AmountScale.of(6)

        assertEquals(6, scale.decimalPlaces)
        assertEquals(1_000_000L, scale.scaleFactor)
    }

    @Test
    fun negativeDecimalPlacesShouldFail() {
        assertFailsWith<ArithmeticException> {
            AmountScale.of(-1)
        }
    }

    @Test
    fun tooLargeDecimalPlacesShouldOverflow() {
        assertFailsWith<ArithmeticException> {
            AmountScale.of(19)
        }
    }

    @Test
    fun fromWholeUnitsShouldScaleCorrectly() {
        val scale = AmountScale.Cents

        val amount = scale.fromWholeUnits(12)

        assertEquals(1200L, amount.scaledUnits)
    }

    @Test
    fun fromWholeUnitsShouldSupportZero() {
        val scale = AmountScale.Milli

        val amount = scale.fromWholeUnits(0)

        assertEquals(0L, amount.scaledUnits)
    }

    @Test
    fun fromWholeUnitsShouldRejectNegativeInput() {
        val scale = AmountScale.Cents

        assertFailsWith<IllegalArgumentException> {
            scale.fromWholeUnits(-1)
        }
    }

    @Test
    fun fromWholeUnitsShouldFailOnOverflow() {
        val scale = AmountScale.Cents

        assertFailsWith<ArithmeticException> {
            scale.fromWholeUnits(Long.MAX_VALUE)
        }
    }
}