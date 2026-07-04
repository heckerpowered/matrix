/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.gameplay

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.TimeController
import heckerpowered.matrix.client.player
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.world.entity.Entity

object ImminentDanger {
    private val timeController = TimeController.allocateTimeController()
    var trackedEntity: Entity? = null

    init {
        // 26.2: HudRenderCallback was replaced by HudElementRegistry; still invoked once per rendered frame.
        HudElementRegistry.addLast(Matrix.identifier("imminent_danger")) { drawContext, tickCounter ->
            val trackedEntity = this.trackedEntity
            if (trackedEntity != null && trackedEntity.isAlive && trackedEntity.distanceToSqr(player) < 9) {
                timeController.value = 0.05
            } else {
                timeController.value = 1.0
            }
        }
    }
}