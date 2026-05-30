/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.account

import heckerpowered.ledger.amount.BalanceUnits

data class LedgerAccountState(
    val accountId: LedgerAccountId,
    val balanceUnits: BalanceUnits,
)