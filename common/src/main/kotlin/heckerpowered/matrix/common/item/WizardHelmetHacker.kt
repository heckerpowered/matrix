/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item


import heckerpowered.matrix.common.reference.ModItemIds
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.system.Magics
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity

object WizardHelmetHacker : WizardHelmet(
    Properties().setId(ModItemIds.wizardHelmetHacker)
        .rarity(Rarity.EPIC)
        // The pre-refactor mana system gave every player a helmet-independent 100-point pool;
        // the ledger refactor made max mana helmet-derived but never assigned this dev helmet
        // a capacity, leaving it at 0/0. 100 mirrors the old pool base.
        .maxMana(100.0)
) {

    override fun getMagics(player: Player, itemStack: ItemStack): Sequence<Magic> {
        return Magics.asSequence()
    }

    override fun hasMagic(player: Player, itemStack: ItemStack, magic: Magic): Boolean {
        return true
    }
}