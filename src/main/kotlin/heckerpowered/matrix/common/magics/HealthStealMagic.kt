package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

class HealthStealMagic : Magic(
    MatrixLanguage.magicHealthSteal,
    8,
    MatrixLanguage.magicHealthStealDescription,
    20
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        if (player == null) {
            return
        }

        val amount = target.maxHealth * 0.5F
        player.absorptionAmount += amount
        player.heal(amount * 0.5f)
        player.hungerManager.add((amount * 0.5).toInt(), amount * 0.5F)
    }
}