/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.MagicManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Rarity

object WizardHelmetHacker : WizardHelmet(
    100.0,
    Settings()
        .fireproof()
        .rarity(Rarity.EPIC)
) {
    override fun getMagics(player: PlayerEntity, itemStack: ItemStack): List<Magic> {
        return MagicManager.getRegisteredMagics()
    }
}