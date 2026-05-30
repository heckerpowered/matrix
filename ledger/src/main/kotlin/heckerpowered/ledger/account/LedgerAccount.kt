/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.account

import heckerpowered.ledger.Ledger
import heckerpowered.ledger.Ledger.Companion.LedgerApproval
import heckerpowered.ledger.amount.BalanceUnits
import heckerpowered.ledger.transaction.*
import heckerpowered.ledger.transaction.constraint.*

class LedgerAccount internal constructor(
    private val ledger: Ledger,
    val id: LedgerAccountId,
) {
    private var cachedBalanceUnits = 0L
    var transactionConstraints: Set<TransactionConstraint> = mutableSetOf()

    internal fun applyApprovedTransaction(@Suppress("unused") approval: LedgerApproval, transaction: LedgerTransaction) {
        val postings = transaction.postingsMap[id] ?: return
        val deltaUnits = postings.fold(0L) { balanceUnits, posting ->
            Math.addExact(balanceUnits, posting.deltaUnits)
        }
        cachedBalanceUnits = Math.addExact(cachedBalanceUnits, deltaUnits)
    }

    fun asState(): LedgerAccountState = LedgerAccountState(id, cachedBalanceUnits)

    fun previewState(transaction: LedgerTransaction): LedgerAccountState {
        val postings = transaction.postingsMap[id].orEmpty()

        val deltaUnits = postings.fold(0L) { totalDeltaUnits, posting ->
            Math.addExact(totalDeltaUnits, posting.deltaUnits)
        }
        val balanceUnits = Math.addExact(cachedBalanceUnits, deltaUnits)

        return LedgerAccountState(id, balanceUnits)
    }

    fun createConstraintContext(transaction: LedgerTransaction): TransactionConstraintContext {
        val previousState = asState()
        val nextState = previewState(transaction)
        return TransactionConstraintContext(transaction, previousState, nextState)
    }

    fun checkTransaction(transaction: LedgerTransaction): TransactionResult {
        val context = createConstraintContext(transaction)
        val violatedConstraint = transactionConstraints.firstOrNull { !it.isSatisfied(context) }
        return when (violatedConstraint) {
            null -> TransactionResult.Approved
            else -> TransactionResult.ConstraintViolation(violatedConstraint, context.previousState, context.nextState)
        }
    }

    fun postings(): Sequence<LedgerPosting> {
        return ledger.transactions()
            .asSequence()
            .flatMap { transaction -> transaction.postings.asSequence() }
            .filter { posting -> posting.accountId == id }
    }

    fun cachedBalanceUnits(): BalanceUnits {
        return cachedBalanceUnits
    }

    fun ledgerBalanceUnits(): BalanceUnits {
        return postings().fold(0L) { balanceUnits, posting ->
            Math.addExact(balanceUnits, posting.deltaUnits)
        }
    }

    fun verifyBalance() {
        require(cachedBalanceUnits() == ledgerBalanceUnits()) {
            "Cached balance does not match ledger balance for $id"
        }
    }

    fun transfer(to: LedgerAccount, amountUnits: BalanceUnits): Transaction {
        require(ledger === to.ledger) { "Transfer must be between accounts in the same ledger" }
        return LedgerTransactions.transfer(id, to.id, amountUnits)
    }

    fun postTransfer(to: LedgerAccount, amountUnits: BalanceUnits): TransactionResult {
        return ledger.postTransaction(transfer(to, amountUnits))
    }
}

fun LedgerAccount.incomingLimitUnits(): BalanceUnits? {
    return transactionConstraints
        .filterIsInstance<BoundedTransactionConstraint>()
        .minOfOrNull { it.effectiveMaximumBalanceUnits }
}

fun LedgerAccount.outgoingLimitUnits(): BalanceUnits? {
    val boundedOutgoingLimitUnits = transactionConstraints
        .filterIsInstance<BoundedTransactionConstraint>()
        .maxOfOrNull { it.effectiveMinimumBalanceUnits }

    val noDebtLimitUnits: BalanceUnits? =
        if (transactionConstraints.any { it is NoDebtTransactionConstraint }) 0 else null

    return listOfNotNull(
        boundedOutgoingLimitUnits,
        noDebtLimitUnits,
    ).maxOrNull()
}

fun LedgerAccount.incomingRemainingUnits(): BalanceUnits? {
    val limitUnits = incomingLimitUnits() ?: return null
    return (limitUnits - cachedBalanceUnits()).coerceAtLeast(0L)
}

fun LedgerAccount.outgoingRemainingUnits(): BalanceUnits? {
    val limitUnits = outgoingLimitUnits() ?: return null
    return (cachedBalanceUnits() - limitUnits).coerceAtLeast(0L)
}

fun BalanceUnits.coerceTransferUnits(
    source: LedgerAccount,
    destination: LedgerAccount,
): BalanceUnits {
    val requestedUnits = this
    if (requestedUnits <= 0L) {
        return 0L
    }

    val sourceOutgoingRemainingUnits = source.outgoingRemainingUnits()
    val destinationIncomingRemainingUnits = destination.incomingRemainingUnits()

    return sequenceOf(
        requestedUnits,
        sourceOutgoingRemainingUnits,
        destinationIncomingRemainingUnits,
    )
        .filterNotNull()
        .min()
        .coerceAtLeast(0L)
}