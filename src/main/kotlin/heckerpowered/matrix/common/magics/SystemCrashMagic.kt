package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.network.SystemCrashPayload
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object SystemCrashMagic : Magic(MatrixLanguage.magicSystemCrash, 100, MatrixLanguage.magicSystemCrashDescription, 200) {

    override fun channel(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        if (target is ServerPlayerEntity) {
            ServerPlayNetworking.send(target, SystemCrashPayload())
        }
    }
}