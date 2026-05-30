/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction.constraint

data object NoDebtTransactionConstraint : TransactionConstraint {
    override fun isSatisfied(context: TransactionConstraintContext): Boolean {
        return context.nextState.balanceUnits >= 0L
    }
}