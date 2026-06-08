/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item


import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.system.Magics
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity

object WizardHelmetHacker : WizardHelmet(
    Properties().setId(heckerpowered.matrix.common.reference.ModItemIds.wizardHelmetHacker)
        .rarity(Rarity.EPIC)
) {

    override fun getMagics(player: Player, itemStack: ItemStack): Sequence<Magic> {
        return Magics.asSequence()
    }

    override fun hasMagic(player: Player, itemStack: ItemStack, magic: Magic): Boolean {
        return true
    }
}