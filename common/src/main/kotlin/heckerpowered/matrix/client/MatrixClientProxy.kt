/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import heckerpowered.matrix.MatrixCommonProxy
import heckerpowered.matrix.common.magic.resource.Mana
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerPlayer

class MatrixClientProxy : MatrixCommonProxy() {
    override fun getPlayerMana(player: Player): Mana {
        if (player is ServerPlayer) {
            return super.getPlayerMana(player)
        }
        if (minecraft.player == null || player != ::player.get()) {
            return .0.mana
        }

        return (MatrixHud.mana - MatrixHud.manaUsage).mana
    }

    override fun getPlayerMaxMana(player: Player): Mana {
        if (player is ServerPlayer) {
            return super.getPlayerMaxMana(player)
        }
        if (minecraft.player == null || player != ::player.get()) {
            return .0.mana
        }

        return MatrixHud.maxMana.mana
    }

    override fun isInfiniteMana(player: Player): Boolean {
        if (player is ServerPlayer) {
            return super.isInfiniteMana(player)
        }
        return false
    }
}
