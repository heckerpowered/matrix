/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction.constraint

fun interface TransactionConstraint {
    fun isSatisfied(context: TransactionConstraintContext): Boolean
}