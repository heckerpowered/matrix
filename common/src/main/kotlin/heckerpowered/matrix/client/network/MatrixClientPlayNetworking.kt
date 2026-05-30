/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.network

import heckerpowered.matrix.common.network.*
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object MatrixClientPlayNetworking {
    fun onInitialize() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSyncManaPayload.id, ClientboundSyncManaPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSystemCrashPayload.id, ClientboundSystemCrashPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundChannelMagicPayload.id, ClientboundChannelMagicPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundBorrowedTimePayload.id, ClientboundBorrowedTimePayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSyncHealthPayload.id, ClientboundSyncHealthPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundWitherArmorTriggerPayload.id, ClientboundWitherArmorTriggerPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ImminentDangerPayload.id, ImminentDangerPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundTeleportPayload.type, ClientboundTeleportPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundExplosionPayload.id, ClientboundExplosionPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundDamageNumberPayload.id, ClientboundDamageNumberPayload::handle)
    }
}