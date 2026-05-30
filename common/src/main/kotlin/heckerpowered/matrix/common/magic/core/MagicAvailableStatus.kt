/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.core

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier

open class MagicAvailableStatus(val identifier: Identifier) {
    companion object {
        val InsufficientMana = MagicAvailableStatus(Matrix.identifier("insufficient_mana"))
        val TargetImmune = MagicAvailableStatus(Matrix.identifier("target_immune"))
        val Unavailable = MagicAvailableStatus(Matrix.identifier("unavailable"))
        val ChannelQueueFull = MagicAvailableStatus(Matrix.identifier("channel_queue_full"))
        val ChannelQueueLocked = MagicAvailableStatus(Matrix.identifier("channel_queue_locked"))
        val TargetMissing = MagicAvailableStatus(Matrix.identifier("target_missing"))
    }

    open val description: MutableComponent = Component.translatable("matrix.magic.available_status.${identifier.path}")
}

data class MagicAvailability(
    private val statuses: LinkedHashSet<MagicAvailableStatus> = linkedSetOf(),
) : Iterable<MagicAvailableStatus> {
    constructor(vararg statuses: MagicAvailableStatus) : this(linkedSetOf(*statuses))

    fun add(status: MagicAvailableStatus) {
        statuses.add(status)
    }

    override fun iterator(): Iterator<MagicAvailableStatus> {
        return statuses.iterator()
    }
}

enum class LMagicAvailableStatus {
    /**
     * The magic is available to be cast.
     */
    AVAILABLE,

    /**
     * The magic is not available to be cast because the player does not have enough mana.
     */
    AVAILABLE_MANA_NOT_ENOUGH,

    /**
     * The target is immune to this magic.
     */
    TARGET_IMMUNE,

    /**
     * The magic is not available for unknown reasons.
     */
    UNAVAILABLE,

    /**
     * The magic channeling queue is full.
     */
    CHANNEL_QUEUE_FULL,

    /**
     * The magic channeling queue is locked.
     */
    CHANNEL_QUEUE_LOCKED,

    /**
     * The magic cannot channel without target
     */
    TARGET_MISSING,

    SCULK_CATALYST_IS_ALREADY_ACTIVE;
}

val LMagicAvailableStatus.description: Component
    get() = when (this) {
        LMagicAvailableStatus.AVAILABLE -> MatrixLanguage.magicAvailable
        LMagicAvailableStatus.AVAILABLE_MANA_NOT_ENOUGH -> MatrixLanguage.magicAvailableManaNotEnough
        LMagicAvailableStatus.TARGET_IMMUNE -> MatrixLanguage.magicTargetImmune
        LMagicAvailableStatus.UNAVAILABLE -> MatrixLanguage.magicUnavailable
        LMagicAvailableStatus.CHANNEL_QUEUE_FULL -> MatrixLanguage.magicChannelQueueFull
        LMagicAvailableStatus.CHANNEL_QUEUE_LOCKED -> MatrixLanguage.magicChannelQueueLocked
        LMagicAvailableStatus.TARGET_MISSING -> MatrixLanguage.magicTargetMissing
        LMagicAvailableStatus.SCULK_CATALYST_IS_ALREADY_ACTIVE -> MatrixLanguage.magicSculkCatalystIsAlreadyActive
    }

