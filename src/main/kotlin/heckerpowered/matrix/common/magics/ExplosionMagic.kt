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

object ExplosionMagic : Magic(MatrixLanguage.magicExplosion, 30, MatrixLanguage.magicExplosionDescription, 30) {
    val explosionBehavior = AdvancedExplosionBehavior(
        false,
        true,
        Optional.empty(),
        Optional.empty()
    )

    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        val damageSource = MemoryEraseMagic.getDamageSource(player, target, sequence) { player?.damageSources?.explosion(target, player) }

        target.world.createExplosion(player, damageSource, explosionBehavior, target.x, target.y, target.z, ((player?.magicClock ?: 1.0) * 4.0).toFloat(), false, World.ExplosionSourceType.MOB)
    }
}