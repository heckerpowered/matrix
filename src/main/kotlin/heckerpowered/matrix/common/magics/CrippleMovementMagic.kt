package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.isInvulnerableTo
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.magicClock
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

class CrippleMovementMagic :
    Magic(
        MatrixLanguage.magicCrippleMovement,
        6,
        MatrixLanguage.magicCrippleMovementDescription,
        2
    ) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        val magicClock = player?.magicClock ?: 1.0
        target.addStatusEffect(
            StatusEffectInstance(
                StatusEffects.SLOWNESS,
                (20 * 5 * magicClock).toInt(),
                (magicClock - 1).toInt() * 5
            )
        )
    }

    override fun availableStatus(
        player: PlayerEntity,
        target: LivingEntity?,
        sequence: ChannelSequence?
    ): MagicAvailableStatus {
        if (target?.isInvulnerableTo(StatusEffects.SLOWNESS) == false) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}