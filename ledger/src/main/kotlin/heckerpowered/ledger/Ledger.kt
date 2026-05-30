/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger

import heckerpowered.ledger.account.LedgerAccount
import heckerpowered.ledger.account.LedgerAccountId
import heckerpowered.ledger.amount.BalanceUnits
import heckerpowered.ledger.transaction.*

class Ledger {
    companion object {
        sealed interface LedgerApproval
        private object Approval : LedgerApproval
    }

    private val transactionJournals = mutableListOf<LedgerTransaction>()
    private val accounts = mutableMapOf<LedgerAccountId, LedgerAccount>()

    fun postTransaction(transaction: LedgerTransaction): TransactionResult {
        val result = checkTransaction(transaction)
        if (result is TransactionResult.Approved) {
            applyApprovedTransaction(transaction)
        }
        return result
    }

    private fun checkTransaction(transaction: LedgerTransaction): TransactionResult {
        if (transaction is CheckpointTransaction || transaction is AdjustmentTransaction) {
            return TransactionResult.Approved
        }

        val affectedAccountIds = transaction.postingsMap.keys
        val transactionResult = affectedAccountIds
            .asSequence()
            .map(::account)
            .map { account -> account.checkTransaction(transaction) }
            .firstOrNull { result -> result !is TransactionResult.Approved }
            ?: TransactionResult.Approved
        return transactionResult
    }

    private fun applyApprovedTransaction(transaction: LedgerTransaction) {
        transactionJournals += transaction

        for (accountId in transaction.postingsMap.keys) {
            account(accountId).applyApprovedTransaction(Approval, transaction)
        }
    }

    fun transactions(): List<LedgerTransaction> {
        return transactionJournals
    }

    fun account(accountId: LedgerAccountId): LedgerAccount = accounts.getOrPut(accountId) {
        LedgerAccount(this, accountId)
    }

    fun getOrInitAccount(accountId: LedgerAccountId, initializer: LedgerAccount.() -> Unit): LedgerAccount = accounts.getOrPut(accountId) {
        val account = LedgerAccount(this, accountId)
        initializer(account)
        account
    }

    fun prune() {
        val balances = rebuildBalances()
        val checkpointPostings = balances
            .asSequence()
            .filter { (_, balanceUnits) -> balanceUnits != 0L }
            .map { (accountId, balanceUnits) -> LedgerPosting(accountId, balanceUnits) }
            .toList()

        val replacementTransactions = if (checkpointPostings.isEmpty()) {
            emptyList()
        } else {
            listOf(CheckpointTransaction(checkpointPostings))
        }

        transactionJournals.clear()
        transactionJournals += replacementTransactions
    }

    fun createCheckpointTransactionFromCache(): CheckpointTransaction? {
        val checkpointPostings = accounts
            .asSequence()
            .filter { (_, account) -> account.cachedBalanceUnits() != 0L }
            .map { (accountId, account) -> LedgerPosting(accountId, account.cachedBalanceUnits()) }
            .toList()

        if (checkpointPostings.isEmpty()) {
            return null
        }

        return CheckpointTransaction(checkpointPostings)
    }

    fun compactFromCache() {
        val checkpointTransaction = createCheckpointTransactionFromCache()

        transactionJournals.clear()

        if (checkpointTransaction != null) {
            transactionJournals += checkpointTransaction
        }
    }

    fun rebuildBalances(): Map<LedgerAccountId, BalanceUnits> =
        transactions()
            .asSequence()
            .flatMap { transaction -> transaction.postings.asSequence() }
            .groupBy { posting -> posting.accountId }
            .mapValues { (_, postings) ->
                postings.fold(0L) { balanceUnits, posting ->
                    Math.addExact(balanceUnits, posting.deltaUnits)
                }
            }

    fun verifyCachedBalances() {
        val rebuiltBalances = rebuildBalances()

        require(rebuiltBalances == accounts) {
            "Cached balances do not match rebuilt balances"
        }
    }
}