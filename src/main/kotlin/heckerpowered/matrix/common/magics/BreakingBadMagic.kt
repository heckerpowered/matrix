package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.isInvulnerableToEffect
import heckerpowered.matrix.common.magics.ExplosionMagic.Companion.explosionBehavior
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World

class BreakingBadMagic :
    Magic(
        MatrixLanguage.magicBreakingBad,
        10,
        MatrixLanguage.magicBreakingBadDescription,
        10
    ) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        target.addStatusEffect(
            StatusEffectInstance(
                StatusEffects.POISON,
                20 * 10,
                4
            )
        )

        target.addStatusEffect(
            StatusEffectInstance(
                StatusEffects.BLINDNESS,
                20 * 10,
                4
            )
        )

        if (target.isOnFire) {
            val damageSource = if (sequence.sequencedAfter<MemoryEraseMagic>()) {
                target.damageSources.generic()
            } else {
                player?.damageSources?.playerAttack(player) ?: target.damageSources.generic()
            }

            target.world.createExplosion(
                player,
                damageSource,
                explosionBehavior,
                target.x,
                target.y,
                target.z,
                4.0F,
                false,
                World.ExplosionSourceType.MOB
            )
        }
    }

    override fun availableStatus(
        player: PlayerEntity,
        target: LivingEntity?,
        sequence: ChannelSequence?
    ): MagicAvailableStatus {
        if (target?.isInvulnerableToEffect(StatusEffects.POISON) == true ||
            target?.isInvulnerableToEffect(StatusEffects.BLINDNESS) == true
        ) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}