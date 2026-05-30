/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction

import heckerpowered.ledger.account.LedgerAccountId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LedgerTransactionsTest {
    @Test
    fun transferShouldCreateBalancedTransaction() {
        val source = LedgerAccountId(1L)
        val target = LedgerAccountId(2L)

        val transaction = LedgerTransactions.transfer(
            from = source,
            to = target,
            amountUnits = 30L,
        )

        val postings = transaction.postings

        assertEquals(2, postings.size)
        assertEquals(source, postings[0].accountId)
        assertEquals(-30L, postings[0].deltaUnits)
        assertEquals(target, postings[1].accountId)
        assertEquals(30L, postings[1].deltaUnits)
    }

    @Test
    fun transferShouldRejectNonPositiveAmount() {
        assertFailsWith<IllegalArgumentException> {
            LedgerTransactions.transfer(
                from = LedgerAccountId(1L),
                to = LedgerAccountId(2L),
                amountUnits = 0L,
            )
        }

        assertFailsWith<IllegalArgumentException> {
            LedgerTransactions.transfer(
                from = LedgerAccountId(1L),
                to = LedgerAccountId(2L),
                amountUnits = -1L,
            )
        }
    }

    @Test
    fun transferShouldRejectSameSourceAndTarget() {
        assertFailsWith<IllegalArgumentException> {
            LedgerTransactions.transfer(
                from = LedgerAccountId(1L),
                to = LedgerAccountId(1L),
                amountUnits = 1L,
            )
        }
    }
}