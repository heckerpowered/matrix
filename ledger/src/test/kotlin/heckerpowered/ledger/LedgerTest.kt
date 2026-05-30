/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger

import heckerpowered.ledger.account.LedgerAccountId
import heckerpowered.ledger.transaction.*
import heckerpowered.ledger.transaction.constraint.BoundedTransactionConstraint
import heckerpowered.ledger.transaction.constraint.NoDebtTransactionConstraint
import kotlin.test.*

class LedgerTest {
    @Test
    fun shouldStartWithEmptyJournal() {
        val ledger = Ledger()

        assertTrue(ledger.transactions().isEmpty())
    }

    @Test
    fun accountShouldStartWithZeroCachedAndLedgerBalance() {
        val ledger = Ledger()
        val account = ledger.account(LedgerAccountId(1L))

        assertEquals(0L, account.cachedBalanceUnits())
        assertEquals(0L, account.ledgerBalanceUnits())
    }

    @Test
    fun shouldPostTransactionToJournal() {
        val ledger = Ledger()

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(LedgerAccountId(1L), 100L),
                    LedgerPosting(LedgerAccountId(2L), -100L),
                ),
            ),
        )

        assertEquals(1, ledger.transactions().size)
    }

    @Test
    fun accountShouldExposeCorrectCachedBalanceAfterAppend() {
        val ledger = Ledger()
        val account1 = ledger.account(LedgerAccountId(1L))
        val account2 = ledger.account(LedgerAccountId(2L))

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, 100L),
                    LedgerPosting(account2.id, -100L),
                ),
            ),
        )

        assertEquals(100L, account1.cachedBalanceUnits())
        assertEquals(-100L, account2.cachedBalanceUnits())
    }

    @Test
    fun accountShouldExposeCorrectLedgerBalanceAfterAppend() {
        val ledger = Ledger()
        val account1 = ledger.account(LedgerAccountId(1L))
        val account2 = ledger.account(LedgerAccountId(2L))

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, 100L),
                    LedgerPosting(account2.id, -100L),
                ),
            ),
        )

        assertEquals(100L, account1.ledgerBalanceUnits())
        assertEquals(-100L, account2.ledgerBalanceUnits())
    }

    @Test
    fun cachedBalanceShouldMatchLedgerBalanceAfterSingleTransaction() {
        val ledger = Ledger()
        val account1 = ledger.account(LedgerAccountId(1L))
        val account2 = ledger.account(LedgerAccountId(2L))

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, 100L),
                    LedgerPosting(account2.id, -100L),
                ),
            ),
        )

        assertEquals(account1.ledgerBalanceUnits(), account1.cachedBalanceUnits())
        assertEquals(account2.ledgerBalanceUnits(), account2.cachedBalanceUnits())
    }

    @Test
    fun cachedBalanceShouldMatchLedgerBalanceAfterMultipleTransactions() {
        val ledger = Ledger()
        val account1 = ledger.account(LedgerAccountId(1L))
        val account2 = ledger.account(LedgerAccountId(2L))

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, 100L),
                    LedgerPosting(account2.id, -100L),
                ),
            ),
        )

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, -40L),
                    LedgerPosting(account2.id, 40L),
                ),
            ),
        )

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, 15L),
                    LedgerPosting(account2.id, -15L),
                ),
            ),
        )

        assertEquals(75L, account1.cachedBalanceUnits())
        assertEquals(-75L, account2.cachedBalanceUnits())

        assertEquals(account1.ledgerBalanceUnits(), account1.cachedBalanceUnits())
        assertEquals(account2.ledgerBalanceUnits(), account2.cachedBalanceUnits())
    }

    @Test
    fun accountShouldExposeOnlyItsOwnPostings() {
        val ledger = Ledger()
        val account1 = ledger.account(LedgerAccountId(1L))
        val account2 = ledger.account(LedgerAccountId(2L))

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, 100L),
                    LedgerPosting(account2.id, -100L),
                ),
            ),
        )

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, -25L),
                    LedgerPosting(account2.id, 25L),
                ),
            ),
        )

        val account1Deltas = account1.postings().map { it.deltaUnits }.toList()
        val account2Deltas = account2.postings().map { it.deltaUnits }.toList()

        assertEquals(listOf(100L, -25L), account1Deltas)
        assertEquals(listOf(-100L, 25L), account2Deltas)
    }

    @Test
    fun createCheckpointTransactionFromCacheShouldReturnNullForEmptyLedger() {
        val ledger = Ledger()

        val checkpointTransaction = ledger.createCheckpointTransactionFromCache()

        assertNull(checkpointTransaction)
    }

    @Test
    fun createCheckpointTransactionFromCacheShouldContainOnlyNonZeroBalances() {
        val ledger = Ledger()

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(LedgerAccountId(1L), 100L),
                    LedgerPosting(LedgerAccountId(2L), -60L),
                    LedgerPosting(LedgerAccountId(3L), -40L),
                ),
            ),
        )

        val checkpointTransaction = ledger.createCheckpointTransactionFromCache()

        requireNotNull(checkpointTransaction)

        val deltasByAccountId = checkpointTransaction.postings.associate {
            it.accountId to it.deltaUnits
        }

        assertEquals(3, checkpointTransaction.postings.size)
        assertEquals(100L, deltasByAccountId[LedgerAccountId(1L)])
        assertEquals(-60L, deltasByAccountId[LedgerAccountId(2L)])
        assertEquals(-40L, deltasByAccountId[LedgerAccountId(3L)])
    }

    @Test
    fun compactFromCacheShouldPreserveCachedAndLedgerBalances() {
        val ledger = Ledger()
        val account1 = ledger.account(LedgerAccountId(1L))
        val account2 = ledger.account(LedgerAccountId(2L))

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, 100L),
                    LedgerPosting(account2.id, -100L),
                ),
            ),
        )

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, -40L),
                    LedgerPosting(account2.id, 40L),
                ),
            ),
        )

        val cachedBalance1Before = account1.cachedBalanceUnits()
        val cachedBalance2Before = account2.cachedBalanceUnits()
        val ledgerBalance1Before = account1.ledgerBalanceUnits()
        val ledgerBalance2Before = account2.ledgerBalanceUnits()

        ledger.compactFromCache()

        assertEquals(cachedBalance1Before, account1.cachedBalanceUnits())
        assertEquals(cachedBalance2Before, account2.cachedBalanceUnits())
        assertEquals(ledgerBalance1Before, account1.ledgerBalanceUnits())
        assertEquals(ledgerBalance2Before, account2.ledgerBalanceUnits())
    }

    @Test
    fun compactFromCacheShouldReplaceHistoryWithCheckpointTransaction() {
        val ledger = Ledger()

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(LedgerAccountId(1L), 100L),
                    LedgerPosting(LedgerAccountId(2L), -100L),
                ),
            ),
        )

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(LedgerAccountId(1L), -40L),
                    LedgerPosting(LedgerAccountId(2L), 40L),
                ),
            ),
        )

        ledger.compactFromCache()

        val transactions = ledger.transactions()

        assertEquals(1, transactions.size)
        assertIs<CheckpointTransaction>(transactions.single())
    }

    @Test
    fun compactFromCacheShouldDropZeroBalanceAccounts() {
        val ledger = Ledger()

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(LedgerAccountId(1L), 100L),
                    LedgerPosting(LedgerAccountId(2L), -100L),
                ),
            ),
        )

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(LedgerAccountId(1L), -100L),
                    LedgerPosting(LedgerAccountId(2L), 100L),
                ),
            ),
        )

        ledger.compactFromCache()

        assertTrue(ledger.transactions().isEmpty())
    }

    @Test
    fun appendAfterCompactionShouldKeepCachedAndLedgerBalancesConsistent() {
        val ledger = Ledger()
        val account1 = ledger.account(LedgerAccountId(1L))
        val account2 = ledger.account(LedgerAccountId(2L))

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, 100L),
                    LedgerPosting(account2.id, -100L),
                ),
            ),
        )

        ledger.compactFromCache()

        ledger.postTransaction(
            Transaction(
                postings = listOf(
                    LedgerPosting(account1.id, -25L),
                    LedgerPosting(account2.id, 25L),
                ),
            ),
        )

        assertEquals(75L, account1.cachedBalanceUnits())
        assertEquals(-75L, account2.cachedBalanceUnits())

        assertEquals(account1.ledgerBalanceUnits(), account1.cachedBalanceUnits())
        assertEquals(account2.ledgerBalanceUnits(), account2.cachedBalanceUnits())
    }

    @Test
    fun transferShouldMoveBalanceBetweenAccounts() {
        val ledger = Ledger()

        val source = ledger.account(LedgerAccountId(1L))
        val target = ledger.account(LedgerAccountId(2L))

        source.transactionConstraints = setOf(NoDebtTransactionConstraint)
        target.transactionConstraints = setOf(NoDebtTransactionConstraint)

        val fundingResult = ledger.postTransaction(
            LedgerTransactions.transfer(
                from = LedgerAccountId(100L),
                to = source.id,
                amountUnits = 100L,
            ),
        )
        assertIs<TransactionResult.Approved>(fundingResult)

        val transferResult = ledger.postTransaction(
            LedgerTransactions.transfer(
                from = source.id,
                to = target.id,
                amountUnits = 30L,
            ),
        )
        assertIs<TransactionResult.Approved>(transferResult)

        assertEquals(70L, source.cachedBalanceUnits())
        assertEquals(30L, target.cachedBalanceUnits())

        assertEquals(source.ledgerBalanceUnits(), source.cachedBalanceUnits())
        assertEquals(target.ledgerBalanceUnits(), target.cachedBalanceUnits())
    }

    @Test
    fun transferShouldRejectWhenSourceWouldEnterDebt() {
        val ledger = Ledger()

        val source = ledger.account(LedgerAccountId(1L))
        val target = ledger.account(LedgerAccountId(2L))

        source.transactionConstraints = setOf(NoDebtTransactionConstraint)

        val result = ledger.postTransaction(
            LedgerTransactions.transfer(
                from = source.id,
                to = target.id,
                amountUnits = 1L,
            ),
        )

        assertIs<TransactionResult.ConstraintViolation>(result)

        assertEquals(0L, source.cachedBalanceUnits())
        assertEquals(0L, target.cachedBalanceUnits())
        assertTrue(ledger.transactions().isEmpty())
    }

    @Test
    fun transferShouldRejectWhenTargetWouldExceedUpperBound() {
        val ledger = Ledger()

        val authority = ledger.account(LedgerAccountId(100L))
        val bounded = ledger.account(LedgerAccountId(1L))

        authority.transactionConstraints = emptySet()
        bounded.transactionConstraints = setOf(
            BoundedTransactionConstraint(
                minimumBalanceUnits = 0L,
                maximumBalanceUnits = 50L,
            ),
        )

        val result = ledger.postTransaction(
            LedgerTransactions.transfer(
                from = authority.id,
                to = bounded.id,
                amountUnits = 60L,
            ),
        )

        assertIs<TransactionResult.ConstraintViolation>(result)

        assertEquals(0L, bounded.cachedBalanceUnits())
        assertTrue(ledger.transactions().isEmpty())
    }

    @Test
    fun transferShouldKeepCachedAndLedgerBalancesConsistentAcrossMultipleTransfers() {
        val ledger = Ledger()

        val authority = ledger.account(LedgerAccountId(100L))
        val first = ledger.account(LedgerAccountId(1L))
        val second = ledger.account(LedgerAccountId(2L))

        first.transactionConstraints = setOf(NoDebtTransactionConstraint)
        second.transactionConstraints = setOf(NoDebtTransactionConstraint)

        assertIs<TransactionResult.Approved>(
            ledger.postTransaction(
                LedgerTransactions.transfer(
                    from = authority.id,
                    to = first.id,
                    amountUnits = 100L,
                ),
            ),
        )

        assertIs<TransactionResult.Approved>(
            ledger.postTransaction(
                LedgerTransactions.transfer(
                    from = first.id,
                    to = second.id,
                    amountUnits = 25L,
                ),
            ),
        )

        assertIs<TransactionResult.Approved>(
            ledger.postTransaction(
                LedgerTransactions.transfer(
                    from = first.id,
                    to = second.id,
                    amountUnits = 10L,
                ),
            ),
        )

        assertEquals(65L, first.cachedBalanceUnits())
        assertEquals(35L, second.cachedBalanceUnits())

        first.verifyBalance()
        second.verifyBalance()
    }

    @Test
    fun transferShouldRemainCorrectAfterCompaction() {
        val ledger = Ledger()

        val authority = ledger.account(LedgerAccountId(100L))
        val first = ledger.account(LedgerAccountId(1L))
        val second = ledger.account(LedgerAccountId(2L))

        first.transactionConstraints = setOf(NoDebtTransactionConstraint)
        second.transactionConstraints = setOf(NoDebtTransactionConstraint)

        assertIs<TransactionResult.Approved>(
            ledger.postTransaction(
                LedgerTransactions.transfer(
                    from = authority.id,
                    to = first.id,
                    amountUnits = 100L,
                ),
            ),
        )

        assertIs<TransactionResult.Approved>(
            ledger.postTransaction(
                LedgerTransactions.transfer(
                    from = first.id,
                    to = second.id,
                    amountUnits = 40L,
                ),
            ),
        )

        val firstCachedBefore = first.cachedBalanceUnits()
        val secondCachedBefore = second.cachedBalanceUnits()
        val firstLedgerBefore = first.ledgerBalanceUnits()
        val secondLedgerBefore = second.ledgerBalanceUnits()

        ledger.compactFromCache()

        assertEquals(firstCachedBefore, first.cachedBalanceUnits())
        assertEquals(secondCachedBefore, second.cachedBalanceUnits())
        assertEquals(firstLedgerBefore, first.ledgerBalanceUnits())
        assertEquals(secondLedgerBefore, second.ledgerBalanceUnits())

        first.verifyBalance()
        second.verifyBalance()
    }

    @Test
    fun transferShouldStillWorkAfterCompaction() {
        val ledger = Ledger()

        val authority = ledger.account(LedgerAccountId(100L))
        val first = ledger.account(LedgerAccountId(1L))
        val second = ledger.account(LedgerAccountId(2L))

        first.transactionConstraints = setOf(NoDebtTransactionConstraint)
        second.transactionConstraints = setOf(NoDebtTransactionConstraint)

        assertIs<TransactionResult.Approved>(
            ledger.postTransaction(
                LedgerTransactions.transfer(
                    from = authority.id,
                    to = first.id,
                    amountUnits = 100L,
                ),
            ),
        )

        ledger.compactFromCache()

        assertIs<TransactionResult.Approved>(
            ledger.postTransaction(
                LedgerTransactions.transfer(
                    from = first.id,
                    to = second.id,
                    amountUnits = 25L,
                ),
            ),
        )

        assertEquals(75L, first.cachedBalanceUnits())
        assertEquals(25L, second.cachedBalanceUnits())

        first.verifyBalance()
        second.verifyBalance()
    }
}