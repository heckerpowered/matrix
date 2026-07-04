/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.system

import heckerpowered.ledger.Ledger
import heckerpowered.ledger.account.LedgerAccount
import heckerpowered.ledger.account.LedgerAccountIds
import heckerpowered.ledger.amount.AmountScale
import heckerpowered.ledger.transaction.TransactionResult
import heckerpowered.ledger.transaction.constraint.NoDebtTransactionConstraint
import heckerpowered.matrix.common.magic.resource.Mana
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import net.minecraft.world.entity.LivingEntity
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object ManaLedger {
    val Ledger = Ledger()
    val Authority = Ledger.account(LedgerAccountIds.MonetaryAuthority)
    val AmountScale = AmountScale(2)

    fun issueMana(target: LivingEntity, amount: Mana): TransactionResult {
        val account = account(target)
        return transferMana(Authority, account, amount)
    }

    fun extinguishMana(target: LivingEntity, amount: Mana): TransactionResult {
        val account = account(target)
        return transferMana(account, Authority, amount)
    }

    private fun transferMana(from: LedgerAccount, to: LedgerAccount, amount: Mana): TransactionResult {
        val ledgerAmount = amount.toLedgerUnits()
        if (ledgerAmount <= 0) {
            // The ledger contract rejects non-positive transfers; issuing/extinguishing zero
            // mana (e.g. proportional issuance for a player without a wizard helmet, whose
            // max mana is 0) is a no-op, not an error.
            return TransactionResult.Approved
        }
        return from.postTransfer(to, ledgerAmount)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun account(target: LivingEntity): LedgerAccount {
        val uuid = target.uuid
        val accountId = LedgerAccountIds.fromUuid(Uuid.fromLongs(uuid.mostSignificantBits, uuid.leastSignificantBits))
        val targetAccount = Ledger.getOrInitAccount(accountId) {
            transactionConstraints = setOf(NoDebtTransactionConstraint)
        }
        return targetAccount
    }

    fun mana(target: LivingEntity): Mana {
        val account = account(target)
        val balanceUnits = account.cachedBalanceUnits()
        return AmountScale.toDouble(balanceUnits).mana
    }

    fun Mana.toLedgerUnits(): Long {
        val value = toDouble()

        require(value.isFinite()) { "Mana amount must be finite" }
        require(value >= 0.0) { "Mana amount must be non-negative" }

        return BigDecimal.valueOf(value)
            .movePointRight(AmountScale.decimalPlaces)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }
}