/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction

class CheckpointTransaction(
    postings: List<LedgerPosting>,
) : LedgerTransaction(postings) {
    init {
        require(postings.all { it.deltaUnits != 0L }) {
            "Checkpoint transaction must not contain zero postings"
        }
    }
}