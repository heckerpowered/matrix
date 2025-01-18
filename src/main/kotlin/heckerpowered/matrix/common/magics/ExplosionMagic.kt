package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.magicClock
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World
import net.minecraft.world.explosion.AdvancedExplosionBehavior
import java.util.*

class ExplosionMagic : Magic(
    MatrixLanguage.magicExplosion,
    30,
    MatrixLanguage.magicExplosionDescription,
    30
) {
    companion object {
        val explosionBehavior = AdvancedExplosionBehavior(
            false,
            true,
            Optional.empty(),
            Optional.empty()
        )
    }

    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
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
            ((player?.magicClock ?: 1.0) * 4.0).toFloat(),
            false,
            World.ExplosionSourceType.MOB
        )
    }
}