/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer

val minecraft: Minecraft
    get() = Minecraft.getInstance()

val player: LocalPlayer?
    get() = minecraft.player
