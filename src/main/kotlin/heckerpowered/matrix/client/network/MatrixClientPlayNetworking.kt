/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.network

import heckerpowered.matrix.common.network.*
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object MatrixClientPlayNetworking {
    fun onInitialize() {
        ClientPlayNetworking.registerGlobalReceiver(SyncManaPayload.id, SyncManaPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(SystemCrashPayload.id, SystemCrashPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ChannelMagicPayload.id, ChannelMagicPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ClientboundBorrowedTimePayload.id, ClientboundBorrowedTimePayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(SyncHealthPayload.id, SyncHealthPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(WitherArmorTriggerPayload.id, WitherArmorTriggerPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ImminentDangerPayload.id, ImminentDangerPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(TeleportPayload.id, TeleportPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ExplosionPayload.id, ExplosionPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(DamageNumberPayload.id, DamageNumberPayload::handle)
    }
}