/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.gameplay

import heckerpowered.matrix.client.TimeController
import heckerpowered.matrix.client.player
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.world.entity.Entity

object ImminentDanger {
    private val timeController = TimeController.allocateTimeController()
    var trackedEntity: Entity? = null

    init {
        ClientTickEvents.END_CLIENT_TICK.register {
            val player = player ?: return@register
            val trackedEntity = this.trackedEntity
            if (trackedEntity != null && trackedEntity.isAlive && trackedEntity.distanceToSqr(player) < 9) {
                timeController.value = 0.05
            } else {
                timeController.value = 1.0
            }
        }
    }
}
