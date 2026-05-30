/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction

import heckerpowered.ledger.account.LedgerAccountId
import heckerpowered.ledger.amount.DeltaUnits

data class LedgerPosting(
    val accountId: LedgerAccountId,
    val deltaUnits: DeltaUnits,
)