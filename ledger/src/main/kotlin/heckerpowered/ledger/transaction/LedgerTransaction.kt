/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction

import heckerpowered.ledger.account.LedgerAccountId

abstract class LedgerTransaction(
    val postings: List<LedgerPosting>,
) {
    init {
        require(postings.isNotEmpty()) { "Transaction must contain at least one posting" }
        require(isBalanced(postings)) { "Transaction must be balanced" }
    }

    val postingCount: Int
        get() = postings.size

    val postingsMap: Map<LedgerAccountId, List<LedgerPosting>> by lazy {
        postings.groupBy(LedgerPosting::accountId)
    }

    companion object {
        private fun isBalanced(postings: List<LedgerPosting>): Boolean {
            val totalDeltaUnits = postings.fold(0L) { totalDeltaUnits, posting ->
                Math.addExact(totalDeltaUnits, posting.deltaUnits)
            }
            return totalDeltaUnits == 0L
        }
    }
}