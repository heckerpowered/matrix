/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.resource

import heckerpowered.ledger.transaction.LedgerTransactions
import heckerpowered.ledger.transaction.TransactionResult
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.ManaLedger
import heckerpowered.matrix.common.magic.system.ManaLedger.toLedgerUnits
import heckerpowered.matrix.core.mana

class ManaReserve(override val priority: Int = 100) : CastingResource {
    override val allowExhaustion: Boolean = true

    /**
     * Returns the maximum effective mana this resource can provide
     * under the given calculation context.
     *
     * This method must be:
     * - pure (no side effects)
     * - deterministic for the same context
     *
     * If [allowExhaustion] is false, the returned value represents
     * the supremum of an open interval rather than a closed bound.
     */
    override fun availableAmount(context: MagicCalculationContext): Mana {
        val player = context.playerOrNull() ?: return 0.mana
        return player.mana
    }

    /**
     * Consumes the specified effective mana amount from this resource.
     *
     * This method is only invoked during the commit phase on the server side,
     * after affordability has been validated.
     *
     * The provided amount is guaranteed to respect this resource's exhaustion
     * constraints as defined by [allowExhaustion].
     */
    override fun consume(invocation: MagicInvocation, amount: Mana) {
        val caster = invocation.caster.entityOrNull()!!
        val transactionResult = ManaLedger.extinguishMana(caster, amount)
        if (transactionResult !is TransactionResult.Approved) {
            Matrix.LOGGER.error("Failed to consume mana from ManaReserve: transaction failed during consume phase. amount=$amount, transactionResult=$transactionResult")

            val from = ManaLedger.account(caster).id
            val to = ManaLedger.Authority.id
            ManaLedger.Ledger.postTransaction(LedgerTransactions.adjustment(from, to, amount.toLedgerUnits()))
        }
    }
}
