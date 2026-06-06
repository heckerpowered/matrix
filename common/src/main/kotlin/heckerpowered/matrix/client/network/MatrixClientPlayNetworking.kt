/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.network

import heckerpowered.matrix.common.network.*
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object MatrixClientPlayNetworking {
    fun onInitialize() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSyncManaPayload.type) { payload, context -> payload.handle(context) }
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSystemCrashPayload.type) { payload, context -> payload.handle(context) }
        ClientPlayNetworking.registerGlobalReceiver(ClientboundChannelMagicPayload.type) { payload, context -> payload.handle(context) }
        ClientPlayNetworking.registerGlobalReceiver(ClientboundBorrowedTimePayload.type) { payload, context -> payload.handle(context) }
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSyncHealthPayload.type) { payload, context -> payload.handle(context) }
        ClientPlayNetworking.registerGlobalReceiver(ClientboundWitherArmorTriggerPayload.type) { payload, context -> payload.handle(context) }
        ClientPlayNetworking.registerGlobalReceiver(ClientboundTeleportPayload.type) { payload, context -> payload.handle(context) }
        ClientPlayNetworking.registerGlobalReceiver(ClientboundExplosionPayload.type) { payload, context -> payload.handle(context) }
        ClientPlayNetworking.registerGlobalReceiver(ClientboundDamageNumberPayload.type) { payload, context -> payload.handle(context) }
    }
}
