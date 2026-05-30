/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.ledger.account

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


/**
 * Maps external UUIDs into the non-negative account-id space.
 *
 * Negative values are reserved for system accounts.
 *
 * This mapper is stateless and deterministic, but it cannot guarantee
 * collision-free mapping from 128-bit UUIDs into 64-bit longs.
 */
object LedgerAccountIds {
    /**
     * Reserved system account ids.
     */
    val MonetaryAuthority: LedgerAccountId = LedgerAccountId(-1L)

    /**
     * Maps a UUID into the non-negative account-id space.
     *
     * Returned values are always >= 0 and will never collide with the
     * reserved negative system-account range.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun fromUuid(uuid: Uuid): LedgerAccountId {
        uuid.toByteArray()
        val mixed = mixUuidToLong(uuid)
        return LedgerAccountId(mixed and Long.MAX_VALUE)
    }

    /**
     * Returns true if the id belongs to the reserved system-account range.
     */
    fun isSystemAccount(accountId: Long): Boolean {
        return accountId < 0L
    }

    /**
     * Returns true if the id belongs to the UUID-derived account range.
     */
    fun isUuidAccount(accountId: Long): Boolean {
        return accountId >= 0L
    }

    /**
     * Mixes the 128-bit UUID into a 64-bit value.
     *
     * This is deterministic but not injective.
     */
    @OptIn(ExperimentalUuidApi::class)
    private fun mixUuidToLong(uuid: Uuid): Long {
        val value = uuid.toLongs { mostSignificantBits, leastSignificantBits ->
            mostSignificantBits xor rotateLeft(leastSignificantBits, 1)
        }
        return mix64(value)
    }

    /**
     * SplitMix64 finalizer.
     */
    private fun mix64(value: Long): Long {
        var result = value
        result = (result xor (result ushr 30)) * -4658895280553007687L
        result = (result xor (result ushr 27)) * -7723592293110705685L
        result = result xor (result ushr 31)
        return result
    }

    private fun rotateLeft(value: Long, @Suppress("SameParameterValue") distance: Int): Long {
        return (value shl distance) or (value ushr (64 - distance))
    }
}