/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.gameplay

import heckerpowered.matrix.client.TimeController
import heckerpowered.matrix.client.player
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.entity.Entity

object ImminentDanger {
    private val timeController = TimeController.allocateTimeController()
    var trackedEntity: Entity? = null

    init {
        HudRenderCallback.EVENT.register { drawContext, tickCounter ->
            val trackedEntity = this.trackedEntity
            if (trackedEntity != null && trackedEntity.isAlive && trackedEntity.squaredDistanceTo(player) < 9) {
                timeController.value = 0.05
            } else {
                timeController.value = 1.0
            }
        }
    }
}