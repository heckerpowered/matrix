/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common

import heckerpowered.matrix.common.network.*
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object MatrixServerPlayNetworking {
    fun onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(ServerboundUseMagicPayload.type, ServerboundUseMagicPayload.codec)
        PayloadTypeRegistry.serverboundPlay().register(ServerboundWarpPayload.type, ServerboundWarpPayload.codec)
        PayloadTypeRegistry.serverboundPlay().register(ServerboundActivateBloodPactPayload.type, ServerboundActivateBloodPactPayload.codec)
        PayloadTypeRegistry.serverboundPlay().register(ServerboundBorrowedTimePayload.type, ServerboundBorrowedTimePayload.codec)
        PayloadTypeRegistry.serverboundPlay().register(ServerboundOverclockPayload.type, ServerboundOverclockPayload.codec)

        PayloadTypeRegistry.clientboundPlay().register(ClientboundSyncManaPayload.type, ClientboundSyncManaPayload.codec)
        PayloadTypeRegistry.clientboundPlay().register(ClientboundSystemCrashPayload.type, ClientboundSystemCrashPayload.codec)
        PayloadTypeRegistry.clientboundPlay().register(ClientboundChannelMagicPayload.type, ClientboundChannelMagicPayload.codec)
        PayloadTypeRegistry.clientboundPlay().register(ClientboundBorrowedTimePayload.type, ClientboundBorrowedTimePayload.codec)
        PayloadTypeRegistry.clientboundPlay().register(ClientboundSyncHealthPayload.type, ClientboundSyncHealthPayload.codec)
        PayloadTypeRegistry.clientboundPlay().register(ClientboundWitherArmorTriggerPayload.type, ClientboundWitherArmorTriggerPayload.codec)
        PayloadTypeRegistry.clientboundPlay().register(ClientboundTeleportPayload.type, ClientboundTeleportPayload.codec)
        PayloadTypeRegistry.clientboundPlay().register(ClientboundExplosionPayload.type, ClientboundExplosionPayload.codec)
        PayloadTypeRegistry.clientboundPlay().register(ClientboundDamageNumberPayload.type, ClientboundDamageNumberPayload.codec)

        ServerPlayNetworking.registerGlobalReceiver(ServerboundUseMagicPayload.type, ServerboundUseMagicPayload::handle)
        ServerPlayNetworking.registerGlobalReceiver(ServerboundWarpPayload.type, ServerboundWarpPayload::handle)
        ServerPlayNetworking.registerGlobalReceiver(ServerboundActivateBloodPactPayload.type, ServerboundActivateBloodPactPayload::handle)
        ServerPlayNetworking.registerGlobalReceiver(ServerboundBorrowedTimePayload.type, ServerboundBorrowedTimePayload::handle)
        ServerPlayNetworking.registerGlobalReceiver(ServerboundOverclockPayload.type, ServerboundOverclockPayload::handle)
    }
}
