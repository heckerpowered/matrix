/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.world.item.Rarity

/**
 * Wizard Helmet 1 'Basic'
 */
object WizardHelmet1 : WizardHelmet(
    Properties().setId(ModItemIds.wizardHelmet1)
        .rarity(Rarity.COMMON)
        .maxMana(8.0)
        .maxLoad(105.0)
)