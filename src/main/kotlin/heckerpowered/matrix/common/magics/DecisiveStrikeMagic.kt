package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.magicClock
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

class DecisiveStrikeMagic : Magic(
    MatrixLanguage.magicDecisiveStrike,
    6,
    MatrixLanguage.magicDecisiveStrikeDescription,
    6
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        val damageSource = if (sequence.sequencedAfter<MemoryEraseMagic>()) {
            target.damageSources.magic()
        } else {
            player?.damageSources?.indirectMagic(player, player) ?: target.damageSources.magic()
        }

        target.timeUntilRegen = 0
        target.damage(damageSource, ((player?.magicClock ?: 1.0) * 6.0).toFloat())
    }

    override fun availableStatus(
        player: PlayerEntity,
        target: LivingEntity?,
        sequence: ChannelSequence?
    ): MagicAvailableStatus {
        val damageSource = if (sequence?.sequencedAfter<MemoryEraseMagic>() == true) {
            player.damageSources.magic()
        } else {
            player.damageSources.indirectMagic(player, player)
        }

        if (target?.isInvulnerable == true || target?.isInvulnerableTo(damageSource) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}