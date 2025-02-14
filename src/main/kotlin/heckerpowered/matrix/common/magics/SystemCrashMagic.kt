package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.network.SystemCrashPayload
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object SystemCrashMagic : Magic(MatrixLanguage.magicSystemCrash, 100, MatrixLanguage.magicSystemCrashDescription, 200) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
    }

    override fun channel(player: ServerPlayerEntity, target: LivingEntity, sequence: ChannelSequence) {
        if (target is ServerPlayerEntity) {
            ServerPlayNetworking.send(target, SystemCrashPayload())
        }
    }
}