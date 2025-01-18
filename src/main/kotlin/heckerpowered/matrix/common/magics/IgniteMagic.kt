package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.magics.ExplosionMagic.Companion.explosionBehavior
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World

class IgniteMagic : Magic(
    MatrixLanguage.magicIgniteMagic,
    9,
    MatrixLanguage.magicIgniteMagicDescription,
    9
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        target.setOnFireFor(10F)
        if (target.hasStatusEffect(StatusEffects.POISON)) {
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
        if (target?.isFireImmune == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}