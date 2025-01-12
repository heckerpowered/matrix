package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.effect.MatrixStatusEffects
import heckerpowered.matrix.common.isInvulnerableTo
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.magicClock
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity

class ManaOverloadMagic : Magic(
    MatrixLanguage.magicManaOverload,
    10,
    MatrixLanguage.magicManaOverloadDescription,
    10
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        target.addStatusEffect(
            StatusEffectInstance(
                Registries.STATUS_EFFECT.getEntry(MatrixStatusEffects.manaOverload),
                20 * 5,
                ((player?.magicClock ?: 1.0) - 1).toInt(),
                true,
                false
            )
        )
    }

    override fun availableStatus(
        player: PlayerEntity,
        target: LivingEntity?,
        sequence: ChannelSequence?
    ): MagicAvailableStatus {
        if (target?.isInvulnerableTo(Registries.STATUS_EFFECT.getEntry(MatrixStatusEffects.manaOverload)) == false) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}