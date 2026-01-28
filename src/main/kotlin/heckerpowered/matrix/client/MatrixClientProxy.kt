/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import heckerpowered.matrix.common.MatrixCommonProxy
import heckerpowered.matrix.common.magic.resource.Mana
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

class MatrixClientProxy : MatrixCommonProxy() {
    override fun getPlayerMana(player: PlayerEntity): Mana {
        if (player is ServerPlayerEntity) {
            return super.getPlayerMana(player)
        }
        if (minecraft.player == null || player != ::player.get()) {
            return .0.mana
        }

        return (MatrixHud.mana - MatrixHud.manaUsage).mana
    }

    override fun getPlayerMaxMana(player: PlayerEntity): Mana {
        if (player is ServerPlayerEntity) {
            return super.getPlayerMaxMana(player)
        }
        if (minecraft.player == null || player != ::player.get()) {
            return .0.mana
        }

        return MatrixHud.maxMana.mana
    }

    override fun isInfiniteMana(player: PlayerEntity): Boolean {
        if (player is ServerPlayerEntity) {
            return super.isInfiniteMana(player)
        }
        return false
    }
}