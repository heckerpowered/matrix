/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.item

import net.minecraft.util.Rarity

/**
 * Wizard Helmet 2 'Ruin'
 */
object WizardHelmet2 : WizardHelmet(
    9.0,
    Settings()
        .fireproof()
        .rarity(Rarity.COMMON)
        .component(MatrixComponents.MAX_LOAD, 10.0)
)