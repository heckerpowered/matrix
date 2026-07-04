/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.network

import heckerpowered.matrix.common.network.*
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object MatrixClientPlayNetworking {
    fun onInitialize() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSyncManaPayload.type, ClientboundSyncManaPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSystemCrashPayload.type) { _, context -> ClientboundSystemCrashPayload.handle(context) }
        ClientPlayNetworking.registerGlobalReceiver(ClientboundChannelMagicPayload.type, ClientboundChannelMagicPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundBorrowedTimePayload.type, ClientboundBorrowedTimePayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSyncHealthPayload.type, ClientboundSyncHealthPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundWitherArmorTriggerPayload.type) { _, context -> ClientboundWitherArmorTriggerPayload.handle(context) }
        // ImminentDangerPayload does not exist in the codebase (pre-migration dangling reference):
        // ClientPlayNetworking.registerGlobalReceiver(ImminentDangerPayload.id, ImminentDangerPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundTeleportPayload.type, ClientboundTeleportPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundExplosionPayload.type, ClientboundExplosionPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundDamageNumberPayload.type, ClientboundDamageNumberPayload::handle)
    }
}