package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.isInvulnerableTo
import heckerpowered.matrix.common.magics.ExplosionMagic.Companion.explosionBehavior
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.magicClock
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
        val magicClock = player?.magicClock ?: 1.0
        target.addStatusEffect(
            StatusEffectInstance(
                StatusEffects.POISON,
                (20 * 5 * magicClock).toInt(),
                (magicClock - 1).toInt()
            )
        )

        if (target.isOnFire) {
            val damageSource = if (sequence.sequencedAfter<MemoryEraseMagic>()) {
                target.damageSources.magic()
            } else {
                player?.damageSources?.indirectMagic(player, null) ?: target.damageSources.magic()
            }

            target.world.createExplosion(
                player,
                damageSource,
                explosionBehavior,
                target.x,
                target.y,
                target.z,
                ((player?.magicClock ?: 1.0) * 4.0).toFloat(),
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
        if (target?.isInvulnerableTo(StatusEffects.POISON) == false) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}