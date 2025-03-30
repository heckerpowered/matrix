package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.core.*
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object PullMagic : Magic(MatrixLanguage.magicPull, 20, MatrixLanguage.magicPullDescription, 20) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        val radius = 12.0
        val strength = 2
        for (entity in target.world.getOtherEntities(target, target.pos.toBox().expand(radius))) {
            if (entity == player) {
                return
            }

            val direction = target.pos - entity.pos
            val distance = direction.horizontalLength()
            val force = strength * distance.inverseLerp(.0..radius)
            val normalizedDirection = direction.normalize()
            val velocity = normalizedDirection * force
            entity.velocity += velocity.add(.0, 0.4, 0.0)
            entity.velocityModified = true
        }
    }
}