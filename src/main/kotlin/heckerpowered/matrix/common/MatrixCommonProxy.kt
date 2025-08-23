/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common

import heckerpowered.matrix.common.persistent.mana
import heckerpowered.matrix.common.persistent.maxMana
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

open class MatrixCommonProxy {
    open fun getPlayerMana(player: PlayerEntity): Double {
        if (player !is ServerPlayerEntity) {
            return .0
        }

        return player.mana
    }

    open fun getPlayerMaxMana(player: PlayerEntity): Double {
        if (player !is ServerPlayerEntity) {
            return .0
        }

        return player.maxMana
    }
}