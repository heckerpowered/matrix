/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import heckerpowered.matrix.common.item.ModComponents
import heckerpowered.matrix.common.magic.resource.Mana
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.ManaLedger
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import net.minecraft.world.entity.player.Player

val Player.mana: Mana
    get() = ManaLedger.mana(this)

val Player.maxMana: Mana
    get() = wizardHelmet?.getMaxMana(this, wizardHelmetStack)?.mana ?: 0.mana

val Player.isInfiniteMana: Boolean
    get() = wizardHelmetStack[ModComponents.infiniteMana] ?: false