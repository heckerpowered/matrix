/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction.constraint

import heckerpowered.ledger.account.LedgerAccountId
import heckerpowered.ledger.account.LedgerAccountState
import heckerpowered.ledger.amount.BalanceUnits
import heckerpowered.ledger.transaction.LedgerPosting
import heckerpowered.ledger.transaction.Transaction
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoundedTransactionConstraintTest {
    private fun context(
        previousBalanceUnits: BalanceUnits,
        nextBalanceUnits: BalanceUnits,
    ): TransactionConstraintContext {
        val accountId = LedgerAccountId(1L)

        return TransactionConstraintContext(
            transaction = Transaction(
                postings = listOf(
                    LedgerPosting(accountId, 1L),
                    LedgerPosting(LedgerAccountId(2L), -1L),
                ),
            ),
            previousState = LedgerAccountState(
                accountId = accountId,
                balanceUnits = previousBalanceUnits,
            ),
            nextState = LedgerAccountState(
                accountId = accountId,
                balanceUnits = nextBalanceUnits,
            ),
        )
    }

    @Test
    fun shouldSupportClosedInterval() {
        val constraint = BoundedTransactionConstraint(
            minimumBalanceUnits = 0L,
            maximumBalanceUnits = 10L,
            minimumInclusion = BoundInclusion.INCLUSIVE,
            maximumInclusion = BoundInclusion.INCLUSIVE,
        )

        assertTrue(constraint.isSatisfied(context(0L, 0L)))
        assertTrue(constraint.isSatisfied(context(0L, 10L)))
        assertFalse(constraint.isSatisfied(context(0L, -1L)))
        assertFalse(constraint.isSatisfied(context(0L, 11L)))
    }

    @Test
    fun shouldSupportLeftOpenRightClosedInterval() {
        val constraint = BoundedTransactionConstraint(
            minimumBalanceUnits = 0L,
            maximumBalanceUnits = 10L,
            minimumInclusion = BoundInclusion.EXCLUSIVE,
            maximumInclusion = BoundInclusion.INCLUSIVE,
        )

        assertTrue(constraint.isSatisfied(context(0L, 0L)))
        assertTrue(constraint.isSatisfied(context(0L, 1L)))
        assertTrue(constraint.isSatisfied(context(0L, 10L)))
        assertFalse(constraint.isSatisfied(context(0L, 11L)))
    }

    @Test
    fun shouldSupportLeftClosedRightOpenInterval() {
        val constraint = BoundedTransactionConstraint(
            minimumBalanceUnits = 0L,
            maximumBalanceUnits = 10L,
            minimumInclusion = BoundInclusion.INCLUSIVE,
            maximumInclusion = BoundInclusion.EXCLUSIVE,
        )

        assertTrue(constraint.isSatisfied(context(0L, 0L)))
        assertTrue(constraint.isSatisfied(context(0L, 9L)))
        assertFalse(constraint.isSatisfied(context(0L, 10L)))
    }

    @Test
    fun shouldSupportOpenInterval() {
        val constraint = BoundedTransactionConstraint(
            minimumBalanceUnits = 0L,
            maximumBalanceUnits = 10L,
            minimumInclusion = BoundInclusion.EXCLUSIVE,
            maximumInclusion = BoundInclusion.EXCLUSIVE,
        )

        assertTrue(constraint.isSatisfied(context(0L, 0L)))
        assertTrue(constraint.isSatisfied(context(0L, 1L)))
        assertTrue(constraint.isSatisfied(context(0L, 9L)))
        assertFalse(constraint.isSatisfied(context(0L, 10L)))
    }

    @Test
    fun shouldAllowRecoveryAboveMaximumEvenIfStillOutOfBounds() {
        val constraint = BoundedTransactionConstraint(
            minimumBalanceUnits = 0L,
            maximumBalanceUnits = 10L,
        )

        assertTrue(constraint.isSatisfied(context(15L, 12L)))
        assertFalse(constraint.isSatisfied(context(15L, 16L)))
    }

    @Test
    fun shouldAllowRecoveryBelowMinimumEvenIfStillOutOfBounds() {
        val constraint = BoundedTransactionConstraint(
            minimumBalanceUnits = 0L,
            maximumBalanceUnits = 10L,
        )

        assertTrue(constraint.isSatisfied(context(-5L, -2L)))
        assertFalse(constraint.isSatisfied(context(-5L, -6L)))
    }
}