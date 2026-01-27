/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.util.Rarity

/**
 * Wizard Helmet 1 'Basic'
 */
object WizardHelmet1 : WizardHelmet(
    8.0,
    Settings()
        .fireproof()
        .rarity(Rarity.COMMON)
        .component(MatrixComponents.MAX_LOAD, 5.0)
)