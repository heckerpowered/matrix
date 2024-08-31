package heckerpowered.matrix.client.network

import heckerpowered.matrix.common.network.SyncManaPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

object MatrixClientPlayNetworking {
    fun onInitialize() {
        PayloadTypeRegistry.playS2C().register(SyncManaPayload.id, SyncManaPayload.codec)

        ClientPlayNetworking.registerGlobalReceiver(SyncManaPayload.id, SyncManaPayload::handle)
    }
}