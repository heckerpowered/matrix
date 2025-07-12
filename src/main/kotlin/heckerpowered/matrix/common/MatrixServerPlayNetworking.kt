package heckerpowered.matrix.common

import heckerpowered.matrix.common.network.*
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object MatrixServerPlayNetworking {
    fun onInitialize() {
        PayloadTypeRegistry.playC2S().register(UseMagicPayload.id, UseMagicPayload.codec)
        PayloadTypeRegistry.playC2S().register(OverclockPayload.id, OverclockPayload.codec)
        PayloadTypeRegistry.playC2S().register(WarpPayload.id, WarpPayload.codec)
        PayloadTypeRegistry.playC2S().register(ActiveBloodPactPayload.id, ActiveBloodPactPayload.codec)
        PayloadTypeRegistry.playC2S().register(BorrowedTimePayload.id, BorrowedTimePayload.codec)

        PayloadTypeRegistry.playS2C().register(SyncManaPayload.id, SyncManaPayload.codec)
        PayloadTypeRegistry.playS2C().register(SystemCrashPayload.id, SystemCrashPayload.codec)
        PayloadTypeRegistry.playS2C().register(ChannelMagicPayload.id, ChannelMagicPayload.codec)
        PayloadTypeRegistry.playS2C().register(ClientboundBorrowedTimePayload.id, ClientboundBorrowedTimePayload.codec)
        PayloadTypeRegistry.playS2C().register(SyncHealthPayload.id, SyncHealthPayload.codec)
        PayloadTypeRegistry.playS2C().register(WitherArmorTriggerPayload.id, WitherArmorTriggerPayload.codec)
        PayloadTypeRegistry.playS2C().register(ImminentDangerPayload.id, ImminentDangerPayload.codec)
        PayloadTypeRegistry.playS2C().register(TeleportPayload.id, TeleportPayload.codec)
        PayloadTypeRegistry.playS2C().register(ExplosionPayload.id, ExplosionPayload.codec)
        PayloadTypeRegistry.playS2C().register(DamageNumberPayload.id, DamageNumberPayload.codec)

        ServerPlayNetworking.registerGlobalReceiver(UseMagicPayload.id, UseMagicPayload::handle)
        ServerPlayNetworking.registerGlobalReceiver(OverclockPayload.id, OverclockPayload::handle)
        ServerPlayNetworking.registerGlobalReceiver(WarpPayload.id, WarpPayload::handle)
        ServerPlayNetworking.registerGlobalReceiver(ActiveBloodPactPayload.id, ActiveBloodPactPayload::handle)
        ServerPlayNetworking.registerGlobalReceiver(BorrowedTimePayload.id, BorrowedTimePayload::handle)
    }
}