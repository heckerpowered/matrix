package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.magicClock
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

class HealthStealMagic : Magic(
    MatrixLanguage.magicHealthSteal,
    20,
    MatrixLanguage.magicHealthStealDescription,
    20
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        if (player == null) {
            return
        }
        player.absorptionAmount += (target.health * (player.magicClock)).toFloat()
    }
}