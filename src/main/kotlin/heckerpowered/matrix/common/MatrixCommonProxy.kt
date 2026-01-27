/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common

import heckerpowered.matrix.common.magic.resource.Mana
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.persistent.isInfiniteMana
import heckerpowered.matrix.common.persistent.mana
import heckerpowered.matrix.common.persistent.maxMana
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

open class MatrixCommonProxy {
    open fun getPlayerMana(player: PlayerEntity): Mana {
        if (player !is ServerPlayerEntity) {
            return .0.mana
        }

        return player.mana
    }

    open fun getPlayerMaxMana(player: PlayerEntity): Mana {
        if (player !is ServerPlayerEntity) {
            return .0.mana
        }

        return player.maxMana
    }

    open fun isInfiniteMana(player: PlayerEntity): Boolean {
        if (player !is ServerPlayerEntity) {
            return false
        }

        return player.isInfiniteMana
    }
}