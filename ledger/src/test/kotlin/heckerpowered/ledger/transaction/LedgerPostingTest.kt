/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction

import heckerpowered.ledger.account.LedgerAccountId
import kotlin.test.Test
import kotlin.test.assertEquals

class LedgerPostingTest {
    @Test
    fun shouldCreatePostingWithPositiveDelta() {
        val posting = LedgerPosting(
            accountId = LedgerAccountId(1L),
            deltaUnits = 100L,
        )

        assertEquals(1L, posting.accountId.value)
        assertEquals(100L, posting.deltaUnits)
    }

    @Test
    fun shouldCreatePostingWithNegativeDelta() {
        val posting = LedgerPosting(
            accountId = LedgerAccountId(2L),
            deltaUnits = -100L,
        )

        assertEquals(2L, posting.accountId.value)
        assertEquals(-100L, posting.deltaUnits)
    }

    @Test
    fun shouldCreatePostingWithZeroDelta() {
        val posting = LedgerPosting(
            accountId = LedgerAccountId(3L),
            deltaUnits = 0L,
        )

        assertEquals(3L, posting.accountId.value)
        assertEquals(0L, posting.deltaUnits)
    }
}