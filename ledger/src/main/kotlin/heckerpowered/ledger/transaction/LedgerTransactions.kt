/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction

import heckerpowered.ledger.account.LedgerAccountId
import heckerpowered.ledger.amount.BalanceUnits

object LedgerTransactions {
    fun <T : LedgerTransaction> transaction(from: LedgerAccountId, to: LedgerAccountId, amountUnits: BalanceUnits, constructor: (List<LedgerPosting>) -> T): T {
        require(amountUnits > 0) { "Transfer amount must be positive" }
        require(from != to) { "Transfer must be between different accounts" }

        return constructor(
            listOf(
                LedgerPosting(from, -amountUnits),
                LedgerPosting(to, amountUnits),
            )
        )
    }

    fun transfer(from: LedgerAccountId, to: LedgerAccountId, amountUnits: BalanceUnits): Transaction {
        return transaction(from, to, amountUnits, ::Transaction)
    }

    fun adjustment(from: LedgerAccountId, to: LedgerAccountId, amountUnits: BalanceUnits): AdjustmentTransaction {
        return transaction(from, to, amountUnits, ::AdjustmentTransaction)
    }
}