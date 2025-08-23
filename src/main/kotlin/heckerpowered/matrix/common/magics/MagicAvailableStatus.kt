/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magics

import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.text.Text

enum class MagicAvailableStatus {
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

val MagicAvailableStatus.description: Text
    get() = when (this) {
        MagicAvailableStatus.AVAILABLE -> MatrixLanguage.magicAvailable
        MagicAvailableStatus.AVAILABLE_MANA_NOT_ENOUGH -> MatrixLanguage.magicAvailableManaNotEnough
        MagicAvailableStatus.TARGET_IMMUNE -> MatrixLanguage.magicTargetImmune
        MagicAvailableStatus.UNAVAILABLE -> MatrixLanguage.magicUnavailable
        MagicAvailableStatus.CHANNEL_QUEUE_FULL -> MatrixLanguage.magicChannelQueueFull
        MagicAvailableStatus.CHANNEL_QUEUE_LOCKED -> MatrixLanguage.magicChannelQueueLocked
        MagicAvailableStatus.TARGET_MISSING -> MatrixLanguage.magicTargetMissing
        MagicAvailableStatus.SCULK_CATALYST_IS_ALREADY_ACTIVE -> MatrixLanguage.magicSculkCatalystIsAlreadyActive
    }

