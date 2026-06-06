/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import net.minecraft.world.phys.Vec3

object ScreenEffectRenderer {
    fun onInitialize() = Unit
    fun beginRenderEntity() = Unit
    fun endRenderEntity() = Unit
    fun spawnParticleAt(position: Vec3, count: Int = 1) = Unit
    fun onWitherArmorEffectApplied() = Unit
}
