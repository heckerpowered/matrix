/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction

import heckerpowered.ledger.account.LedgerAccountState
import heckerpowered.ledger.transaction.constraint.TransactionConstraint

sealed interface TransactionResult {
    data object Approved : TransactionResult

    data class ConstraintViolation(
        val constraint: TransactionConstraint,
        val previousState: LedgerAccountState,
        val nextState: LedgerAccountState,
    ) : TransactionResult
}