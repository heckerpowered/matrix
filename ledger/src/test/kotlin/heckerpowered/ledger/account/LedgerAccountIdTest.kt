/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.account

import kotlin.test.Test
import kotlin.test.assertEquals

class LedgerAccountIdTest {
    @Test
    fun shouldCreateAccountId() {
        val accountId = LedgerAccountId(42L)
        val negativeAccountId = LedgerAccountId(-1L)

        assertEquals(42L, accountId.value)
        assertEquals(-1L, negativeAccountId.value)
    }

    @Test
    fun shouldAllowZeroAccountId() {
        val accountId = LedgerAccountId(0L)

        assertEquals(0L, accountId.value)
    }
}