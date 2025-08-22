/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.core

import heckerpowered.matrix.Matrix
import net.minecraft.entity.player.PlayerEntity

val PlayerEntity.mana: Double
    get() = Matrix.proxy.getPlayerMana(this)

val PlayerEntity.maxMana: Double
    get() = Matrix.proxy.getPlayerMaxMana(this)

