/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import heckerpowered.matrix.common.MatrixCommonProxy
import heckerpowered.matrix.common.magic.Mana
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import net.minecraft.entity.player.PlayerEntity

class MatrixClientProxy : MatrixCommonProxy() {
    override fun getPlayerMana(player: PlayerEntity): Mana {
        if (minecraft.player == null || player != ::player.get()) {
            return .0.mana
        }

        return (MatrixHud.mana - MatrixHud.manaUsage).mana
    }

    override fun getPlayerMaxMana(player: PlayerEntity): Mana {
        if (minecraft.player == null || player != ::player.get()) {
            return .0.mana
        }

        return MatrixHud.maxMana.mana
    }

    override fun isInfiniteMana(player: PlayerEntity): Boolean {
        return false
    }
}