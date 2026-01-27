/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.resource.Mana
import net.minecraft.entity.player.PlayerEntity

val PlayerEntity.mana: Mana
    get() = Matrix.proxy.getPlayerMana(this)

val PlayerEntity.maxMana: Mana
    get() = Matrix.proxy.getPlayerMaxMana(this)

val PlayerEntity.isInfiniteMana: Boolean
    get() = Matrix.proxy.isInfiniteMana(this)