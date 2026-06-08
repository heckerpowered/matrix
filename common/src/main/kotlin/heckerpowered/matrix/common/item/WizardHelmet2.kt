/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.world.item.Rarity

/**
 * Wizard Helmet 2 'Ruin'
 */
object WizardHelmet2 : WizardHelmet(
    Properties().setId(heckerpowered.matrix.common.reference.ModItemIds.wizardHelmet2)
        .rarity(Rarity.COMMON)
        .maxMana(9.0)
        .maxLoad(110.0)
)