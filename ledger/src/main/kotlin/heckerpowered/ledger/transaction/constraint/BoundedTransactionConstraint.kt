/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.transaction.constraint

import heckerpowered.ledger.amount.BalanceUnits

class BoundedTransactionConstraint(
    val minimumBalanceUnits: BalanceUnits,
    val maximumBalanceUnits: BalanceUnits,
    val minimumInclusion: BoundInclusion = BoundInclusion.INCLUSIVE,
    val maximumInclusion: BoundInclusion = BoundInclusion.INCLUSIVE,
) : TransactionConstraint {
    init {
        require(minimumBalanceUnits <= maximumBalanceUnits) { "Minimum balance must not exceed maximum balance" }
    }

    override fun isSatisfied(context: TransactionConstraintContext): Boolean {
        val previousBalanceUnits = context.previousState.balanceUnits
        val nextBalanceUnits = context.nextState.balanceUnits

        val satisfiesMinimum = when (minimumInclusion) {
            BoundInclusion.INCLUSIVE -> nextBalanceUnits >= minimumBalanceUnits
            BoundInclusion.EXCLUSIVE -> nextBalanceUnits > minimumBalanceUnits
        }
        val satisfiesMaximum = when (maximumInclusion) {
            BoundInclusion.INCLUSIVE -> nextBalanceUnits <= maximumBalanceUnits
            BoundInclusion.EXCLUSIVE -> nextBalanceUnits < maximumBalanceUnits
        }

        // This constraint applies to balance transitions, not static balance validity.
        // Unchanged balances are always allowed.
        return when {
            satisfiesMinimum && satisfiesMaximum -> true
            !satisfiesMaximum -> nextBalanceUnits <= previousBalanceUnits
            else /* !satisfiesMinimum */ -> nextBalanceUnits >= previousBalanceUnits
        }
    }
}

val BoundedTransactionConstraint.effectiveMinimumBalanceUnits: BalanceUnits
    get() = when (minimumInclusion) {
        BoundInclusion.INCLUSIVE -> minimumBalanceUnits
        BoundInclusion.EXCLUSIVE -> Math.addExact(minimumBalanceUnits, 1L)
    }

val BoundedTransactionConstraint.effectiveMaximumBalanceUnits: BalanceUnits
    get() = when (maximumInclusion) {
        BoundInclusion.INCLUSIVE -> maximumBalanceUnits
        BoundInclusion.EXCLUSIVE -> Math.subtractExact(maximumBalanceUnits, 1L)
    }