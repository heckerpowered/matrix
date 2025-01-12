package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

class KillMagic : Magic(
    MatrixLanguage.killMagic,
    1000,
    MatrixLanguage.killMagicDescription,
    20 * 10
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        target.health = .0f
        val damageSource = if (sequence.sequencedAfter<MemoryEraseMagic>()) {
            target.damageSources.magic()
        } else {
            player?.damageSources?.indirectMagic(player, player) ?: target.damageSources.magic()
        }
        target.onDeath(damageSource)
    }
}