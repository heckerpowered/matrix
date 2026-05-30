/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction

import heckerpowered.ledger.account.LedgerAccountId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CheckpointTransactionTest {
    @Test
    fun shouldCreateBalancedCheckpointTransaction() {
        val transaction = CheckpointTransaction(
            postings = listOf(
                LedgerPosting(
                    accountId = LedgerAccountId(1L),
                    deltaUnits = 100L,
                ),
                LedgerPosting(
                    accountId = LedgerAccountId(2L),
                    deltaUnits = -60L,
                ),
                LedgerPosting(
                    accountId = LedgerAccountId(3L),
                    deltaUnits = -40L,
                ),
            ),
        )

        assertEquals(3, transaction.postings.size)
    }

    @Test
    fun shouldRejectEmptyCheckpointTransaction() {
        assertFailsWith<IllegalArgumentException> {
            CheckpointTransaction(
                postings = emptyList(),
            )
        }
    }

    @Test
    fun shouldRejectUnbalancedCheckpointTransaction() {
        assertFailsWith<IllegalArgumentException> {
            CheckpointTransaction(
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
}