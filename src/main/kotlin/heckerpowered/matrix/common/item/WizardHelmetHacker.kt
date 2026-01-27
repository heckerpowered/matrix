/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.magic.Magic
import heckerpowered.matrix.common.magic.MagicManager
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

    override fun hasMagic(itemStack: ItemStack, magic: Magic): Boolean {
        return true
    }
}