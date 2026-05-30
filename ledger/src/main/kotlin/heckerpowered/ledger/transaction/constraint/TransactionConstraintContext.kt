/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction.constraint

import heckerpowered.ledger.account.LedgerAccountState
import heckerpowered.ledger.transaction.LedgerTransaction

data class TransactionConstraintContext(
    val transaction: LedgerTransaction,
    val previousState: LedgerAccountState,
    val nextState: LedgerAccountState,
)