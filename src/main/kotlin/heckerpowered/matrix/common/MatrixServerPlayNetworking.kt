package heckerpowered.matrix.common

import heckerpowered.matrix.common.network.UseMagicPayload
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object MatrixServerPlayNetworking {
    fun onInitialize() {
        PayloadTypeRegistry.playC2S().register(UseMagicPayload.id, UseMagicPayload.codec)

        ServerPlayNetworking.registerGlobalReceiver(UseMagicPayload.id, UseMagicPayload::handle)
    }
}