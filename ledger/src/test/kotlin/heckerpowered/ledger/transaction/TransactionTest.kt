/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction

import heckerpowered.ledger.account.LedgerAccountId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransactionTest {
    @Test
    fun shouldCreateBalancedTransactionWithTwoPostings() {
        val transaction = Transaction(
            postings = listOf(
                LedgerPosting(
                    accountId = LedgerAccountId(1L),
                    deltaUnits = 100L,
                ),
                LedgerPosting(
                    accountId = LedgerAccountId(2L),
                    deltaUnits = -100L,
                ),
            )
        )

        assertEquals(2, transaction.postingCount)
    }

    @Test
    fun shouldCreateBalancedTransactionWithMultiplePostings() {
        val transaction = Transaction(
            postings = listOf(
                LedgerPosting(
                    accountId = LedgerAccountId(1L),
                    deltaUnits = 200L,
                ),
                LedgerPosting(
                    accountId = LedgerAccountId(2L),
                    deltaUnits = -50L,
                ),
                LedgerPosting(
                    accountId = LedgerAccountId(3L),
                    deltaUnits = -150L,
                ),
            ),
        )

        assertEquals(3, transaction.postingCount)
    }

    @Test
    fun shouldRejectEmptyTransaction() {
        assertFailsWith<IllegalArgumentException> {
            Transaction(
                postings = emptyList(),
            )
        }
    }

    @Test
    fun shouldRejectUnbalancedTransaction() {
        assertFailsWith<IllegalArgumentException> {
            Transaction(
                postings = listOf(
                    LedgerPosting(
                        accountId = LedgerAccountId(1L),
                        deltaUnits = 100L,
                    ),
                    LedgerPosting(
                        accountId = LedgerAccountId(2L),
                        deltaUnits = -99L,
                    ),
                ),
            )
        }
    }

    @Test
    fun shouldAcceptSingleZeroPostingTransaction() {
        val transaction = Transaction(
            postings = listOf(
                LedgerPosting(
                    accountId = LedgerAccountId(1L),
                    deltaUnits = 0L,
                ),
            ),
        )

        assertEquals(1, transaction.postingCount)
    }

    @Test
    fun shouldFailWhenBalanceCheckOverflowsPositiveDirection() {
        assertFailsWith<ArithmeticException> {
            Transaction(
                postings = listOf(
                    LedgerPosting(
                        accountId = LedgerAccountId(1L),
                        deltaUnits = Long.MAX_VALUE,
                    ),
                    LedgerPosting(
                        accountId = LedgerAccountId(2L),
                        deltaUnits = 1L,
                    ),
                    LedgerPosting(
                        accountId = LedgerAccountId(3L),
                        deltaUnits = Long.MIN_VALUE,
                    ),
                ),
            )
        }
    }

    @Test
    fun shouldFailWhenBalanceCheckOverflowsNegativeDirection() {
        assertFailsWith<ArithmeticException> {
            Transaction(
                postings = listOf(
                    LedgerPosting(
                        accountId = LedgerAccountId(1L),
                        deltaUnits = Long.MIN_VALUE,
                    ),
                    LedgerPosting(
                        accountId = LedgerAccountId(2L),
                        deltaUnits = -1L,
                    ),
                    LedgerPosting(
                        accountId = LedgerAccountId(3L),
                        deltaUnits = Long.MAX_VALUE,
                    ),
                ),
            )
        }
    }
}