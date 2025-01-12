package heckerpowered.matrix.client.network

import heckerpowered.matrix.common.network.ChannelMagicPayload
import heckerpowered.matrix.common.network.SyncManaPayload
import heckerpowered.matrix.common.network.SystemCrashPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object MatrixClientPlayNetworking {
    fun onInitialize() {
        ClientPlayNetworking.registerGlobalReceiver(SyncManaPayload.id, SyncManaPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(SystemCrashPayload.id, SystemCrashPayload::handle)
        ClientPlayNetworking.registerGlobalReceiver(ChannelMagicPayload.id, ChannelMagicPayload::handle)
    }
}