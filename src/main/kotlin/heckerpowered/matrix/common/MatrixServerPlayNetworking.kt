package heckerpowered.matrix.common

import heckerpowered.matrix.common.network.*
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object MatrixServerPlayNetworking {
    fun onInitialize() {
        PayloadTypeRegistry.playC2S().register(UseMagicPayload.id, UseMagicPayload.codec)
        PayloadTypeRegistry.playC2S().register(OverclockPayload.id, OverclockPayload.codec)
        PayloadTypeRegistry.playC2S().register(WrapPayload.id, WrapPayload.codec)

        PayloadTypeRegistry.playS2C().register(SyncManaPayload.id, SyncManaPayload.codec)
        PayloadTypeRegistry.playS2C().register(SystemCrashPayload.id, SystemCrashPayload.codec)
        PayloadTypeRegistry.playS2C().register(ChannelMagicPayload.id, ChannelMagicPayload.codec)

        ServerPlayNetworking.registerGlobalReceiver(UseMagicPayload.id, UseMagicPayload::handle)
        ServerPlayNetworking.registerGlobalReceiver(OverclockPayload.id, OverclockPayload::handle)
        ServerPlayNetworking.registerGlobalReceiver(WrapPayload.id, WrapPayload::handle)
    }
}