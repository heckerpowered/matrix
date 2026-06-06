/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix

import heckerpowered.matrix.common.magic.resource.Mana
import heckerpowered.matrix.core.isInfiniteMana
import heckerpowered.matrix.core.mana
import heckerpowered.matrix.core.maxMana
import net.minecraft.world.entity.player.Player

open class MatrixCommonProxy {
    open fun getPlayerMana(player: Player): Mana {
        return player.mana
    }

    open fun getPlayerMaxMana(player: Player): Mana {
        return player.maxMana
    }

    open fun isInfiniteMana(player: Player): Boolean {
        return player.isInfiniteMana
    }
}
