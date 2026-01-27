/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.magic.Mana.Companion.minus
import heckerpowered.matrix.common.network.SyncHealthPayload
import heckerpowered.matrix.common.persistent.isInfiniteMana
import heckerpowered.matrix.common.persistent.mana
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity

/**
 * Describes a plan for channeling a magic, including optional bypass flags
 * and contextual data.
 *
 * A plan does not perform channeling itself, but evaluates whether
 * conditions are met (e.g. queue state, mana affordability).
 *
 * @property bypassLock whether to ignore a locked channel queue.
 * @property costMana whether channeling should consume mana.
 * @property data additional contextual magic data.
 */
open class ChannelPlan(
    val bypassLock: Boolean = false,
    val costMana: Boolean = true,
    val data: MagicData = MagicData(),
) {
    /**
     * Checks whether the given channel queue is locked for this plan.
     *
     * @return true if the queue is locked and this plan does not bypass the lock.
     */
    open fun isQueueLocked(queue: ChannelQueue): Boolean {
        return !bypassLock && queue.isLocked
    }

    /**
     * Returns whether a magic should be considered available under this plan,
     * possibly relaxing certain constraints (queue lock / mana) that the plan allows.
     *
     * Rules:
     * - AVAILABLE → true
     * - CHANNEL_QUEUE_LOCKED → true only if [bypassLock] is true
     * - AVAILABLE_MANA_NOT_ENOUGH → true only if [costMana] is false
     * - All other statuses → false
     */
    open fun isMagicAvailable(availableStatus: MagicAvailableStatus): Boolean {
        if (availableStatus == MagicAvailableStatus.AVAILABLE) {
            return true
        }
        if (availableStatus == MagicAvailableStatus.CHANNEL_QUEUE_LOCKED && bypassLock) {
            return true
        }
        if (availableStatus == MagicAvailableStatus.AVAILABLE_MANA_NOT_ENOUGH && !costMana) {
            return true
        }

        return false
    }

    /**
     * Pays the channeling cost on the server according to this plan.
     *
     * Semantics:
     * - If [costMana] is false or the player has infinite mana → succeed without changes.
     * - If mana >= cost → deduct mana and succeed.
     * - Else if Blood Pact is active → convert the mana deficit from health;
     *   require final health strictly > 0 to succeed; otherwise fail with no side effects.
     * - Only successful payments mutate state (mana/health) and send sync.
     *
     * @param channeler server-side player who pays.
     * @param cost required mana cost.
     * @param convertRatio health-to-mana conversion ratio for Blood Pact.
     * @return true if paid; false if not enough resources (no side effects on failure).
     */
    open fun payCost(channeler: ServerPlayerEntity, cost: Mana, convertRatio: Double): Boolean {
        if (!costMana || channeler.isInfiniteMana) {
            return true
        }

        if (channeler.mana.amount >= cost.amount) {
            channeler.mana -= cost
            return true
        }

        if (channeler.isBloodPactActive &&
            channeler.mana.amount + channeler.health * convertRatio - cost.amount > 0
        ) {
            val usedHealth = (cost - channeler.mana).amount / convertRatio
            channeler.health = maxOf(channeler.health - usedHealth.toFloat(), 1f)
            channeler.mana = 0.0.mana
            ServerPlayNetworking.send(channeler, SyncHealthPayload(channeler))
            return true
        }

        return false
    }
}