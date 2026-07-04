/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.system

import heckerpowered.matrix.common.network.ClientboundChannelMagicPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity

object MagicSystem {
    fun onInitialize() {
        // Registers the built-in magics; nothing invoked this before, leaving the magic
        // registry empty at runtime (no HUD, no magic enchantments in datagen).
        Magics.onInitialize()
        ChannelQueuePersistenceRule.onInitialize()
        ChannelQueueTicker.onInitialize()
        ManaRegenerationTicker.onInitialize()
        ManaOverclockRule.onInitialize()
    }

    @JvmStatic
    fun onEntityTracked(player: ServerPlayer, entity: Entity) {
        if (entity !is LivingEntity) {
            return
        }

        val channelQueue = entity.channelQueues[player.uuid] ?: return
        channelQueue.channelingMagics().forEach { channelingMagic ->
            ServerPlayNetworking.send(
                player, ClientboundChannelMagicPayload(
                    channelingMagic.magic.definition.uuid,
                    entity.id,
                    channelingMagic.channelTime,
                    channelingMagic.currentChannelTime
                )
            )
        }
    }
}
