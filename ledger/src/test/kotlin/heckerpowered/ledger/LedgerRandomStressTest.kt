/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger

import heckerpowered.ledger.account.LedgerAccount
import heckerpowered.ledger.account.LedgerAccountId
import heckerpowered.ledger.amount.BalanceUnits
import heckerpowered.ledger.transaction.TransactionResult
import heckerpowered.ledger.transaction.constraint.BoundedTransactionConstraint
import heckerpowered.ledger.transaction.constraint.NoDebtTransactionConstraint
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LedgerRandomStressTest {
    @Test
    fun randomStressShouldPreserveAllLedgerInvariants() {
        val random = Random(0x5EED_F00D)
        val ledger = Ledger()

        val authority = ledger.account(LedgerAccountId(1L))

        val boundedAccounts = (0 until 12).map { index ->
            ledger.account(LedgerAccountId((index + 100).toLong())).apply {
                val maximumBalanceUnits = random.nextLong(80L, 600L)
                transactionConstraints = setOf(
                    BoundedTransactionConstraint(
                        minimumBalanceUnits = 0L,
                        maximumBalanceUnits = maximumBalanceUnits,
                    ),
                )
            }
        }

        val unboundedAccounts = (0 until 4).map { index ->
            ledger.account(LedgerAccountId((index + 1000).toLong()))
        }

        val noDebtAccounts = (0 until 4).map { index ->
            ledger.account(LedgerAccountId((index + 2000).toLong())).apply {
                transactionConstraints = setOf(NoDebtTransactionConstraint)
            }
        }

        val allAccounts = buildList {
            add(authority)
            addAll(boundedAccounts)
            addAll(unboundedAccounts)
            addAll(noDebtAccounts)
        }

        repeat(4_000) { step ->
            if (step % 37 == 0) {
                val cachedSnapshotBefore = snapshotBalances(allAccounts)
                val ledgerSnapshotBefore = snapshotLedgerBalances(allAccounts)

                ledger.compactFromCache()

                val cachedSnapshotAfter = snapshotBalances(allAccounts)
                val ledgerSnapshotAfter = snapshotLedgerBalances(allAccounts)

                assertEquals(cachedSnapshotBefore, cachedSnapshotAfter, "Cached balances changed after compaction at step $step")
                assertEquals(ledgerSnapshotBefore, ledgerSnapshotAfter, "Ledger balances changed after compaction at step $step")
                assertTrue(
                    ledger.transactions().size <= 1,
                    "Compaction should leave at most one checkpoint transaction at step $step",
                )

                verifyAllBalances(allAccounts)
                verifyTotalBalanceIsZero(allAccounts)
            }

            val source = pickRandomSource(random, authority, boundedAccounts, unboundedAccounts, noDebtAccounts)
            val target = pickRandomTarget(random, allAccounts, source)

            val amountUnits = random.nextLong(1L, 120L)

            val cachedSnapshotBefore = snapshotBalances(allAccounts)
            val ledgerSnapshotBefore = snapshotLedgerBalances(allAccounts)
            val transactionCountBefore = ledger.transactions().size

            val result = ledger.postTransaction(
                source.transfer(target, amountUnits),
            )

            when (result) {
                TransactionResult.Approved -> {
                    assertEquals(
                        Math.subtractExact(cachedSnapshotBefore.getValue(source.id), amountUnits),
                        source.cachedBalanceUnits(),
                        "Source cached balance mismatch after approved transfer at step $step",
                    )
                    assertEquals(
                        Math.addExact(cachedSnapshotBefore.getValue(target.id), amountUnits),
                        target.cachedBalanceUnits(),
                        "Target cached balance mismatch after approved transfer at step $step",
                    )

                    assertEquals(
                        transactionCountBefore + 1,
                        ledger.transactions().size,
                        "Journal size should increase after approved transfer at step $step",
                    )
                }

                is TransactionResult.ConstraintViolation -> {
                    assertEquals(
                        cachedSnapshotBefore,
                        snapshotBalances(allAccounts),
                        "Cached balances changed after rejected transfer at step $step",
                    )
                    assertEquals(
                        ledgerSnapshotBefore,
                        snapshotLedgerBalances(allAccounts),
                        "Ledger balances changed after rejected transfer at step $step",
                    )
                    assertEquals(
                        transactionCountBefore,
                        ledger.transactions().size,
                        "Journal size changed after rejected transfer at step $step",
                    )
                }
            }

            verifyAllBalances(allAccounts)
            verifyTotalBalanceIsZero(allAccounts)
        }
    }

    @Test
    fun randomRejectedTransfersShouldNeverMutateLedgerState() {
        val random = Random(0xBAD_CAFE)
        val ledger = Ledger()

        val authority = ledger.account(LedgerAccountId(1L))

        val constrainedAccounts = (0 until 8).map { index ->
            ledger.account(LedgerAccountId((index + 500).toLong())).apply {
                transactionConstraints = setOf(
                    BoundedTransactionConstraint(
                        minimumBalanceUnits = 0L,
                        maximumBalanceUnits = 20L,
                    ),
                )
            }
        }

        val allAccounts = buildList {
            add(authority)
            addAll(constrainedAccounts)
        }

        repeat(1_000) { step ->
            val source = constrainedAccounts.random(random)
            val target = constrainedAccounts.filter { it !== source }.random(random)

            val amountUnits = random.nextLong(21L, 100L)

            val cachedSnapshotBefore = snapshotBalances(allAccounts)
            val ledgerSnapshotBefore = snapshotLedgerBalances(allAccounts)
            val transactionCountBefore = ledger.transactions().size

            val result = ledger.postTransaction(
                source.transfer(target, amountUnits),
            )

            assertTrue(
                result is TransactionResult.ConstraintViolation,
                "Expected constraint violation at step $step, but got $result",
            )

            assertEquals(
                cachedSnapshotBefore,
                snapshotBalances(allAccounts),
                "Cached balances changed after rejected transfer at step $step",
            )
            assertEquals(
                ledgerSnapshotBefore,
                snapshotLedgerBalances(allAccounts),
                "Ledger balances changed after rejected transfer at step $step",
            )
            assertEquals(
                transactionCountBefore,
                ledger.transactions().size,
                "Journal size changed after rejected transfer at step $step",
            )

            verifyAllBalances(allAccounts)
            verifyTotalBalanceIsZero(allAccounts)
        }
    }

    private fun verifyAllBalances(accounts: List<LedgerAccount>) {
        for (account in accounts) {
            account.verifyBalance()
            assertEquals(
                account.ledgerBalanceUnits(),
                account.cachedBalanceUnits(),
                "Cached and ledger balances diverged for account ${account.id}",
            )
        }
    }

    private fun verifyTotalBalanceIsZero(accounts: List<LedgerAccount>) {
        val totalBalanceUnits = accounts.fold(0L) { totalBalanceUnits, account ->
            Math.addExact(totalBalanceUnits, account.ledgerBalanceUnits())
        }

        assertEquals(0L, totalBalanceUnits, "Total ledger balance must remain zero")
    }

    private fun snapshotBalances(accounts: List<LedgerAccount>): Map<LedgerAccountId, BalanceUnits> {
        return accounts.associate { account ->
            account.id to account.cachedBalanceUnits()
        }
    }

    private fun snapshotLedgerBalances(accounts: List<LedgerAccount>): Map<LedgerAccountId, BalanceUnits> {
        return accounts.associate { account ->
            account.id to account.ledgerBalanceUnits()
        }
    }

    private fun pickRandomSource(
        random: Random,
        authority: LedgerAccount,
        boundedAccounts: List<LedgerAccount>,
        unboundedAccounts: List<LedgerAccount>,
        noDebtAccounts: List<LedgerAccount>,
    ): LedgerAccount {
        return when (random.nextInt(100)) {
            in 0..24 -> authority
            in 25..59 -> boundedAccounts.random(random)
            in 60..79 -> unboundedAccounts.random(random)
            else -> noDebtAccounts.random(random)
        }
    }

    private fun pickRandomTarget(
        random: Random,
        allAccounts: List<LedgerAccount>,
        source: LedgerAccount,
    ): LedgerAccount {
        return allAccounts.filter { it !== source }.random(random)
    }
}