package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.core.attack
import heckerpowered.matrix.core.squaredDistanceTo
import heckerpowered.matrix.core.toBox
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity

object TeleportMagic : Magic(MatrixLanguage.magicTeleport, 15, MatrixLanguage.magicTeleportDescription, 5) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        if (player == null) {
            return
        }

        val velocity = player.velocity
        player.teleport(target.x, target.y, target.z, true)
        player.networkHandler.sendPacket(EntityVelocityUpdateS2CPacket(player))
        player.velocity = velocity
        player.velocityDirty = true
        player.velocityModified = true

        target.world.getOtherEntities(player, target.pos.toBox().expand(3.0))
            .filter { it squaredDistanceTo player <= 6 * 6 }
            .forEach {
                it.timeUntilRegen = 0
                player.lastAttackedTicks = Int.MAX_VALUE
                player attack it
                player.addCritParticles(it)
                player.addEnchantedHitParticles(it)
                player.swingHand(player.activeHand, true)
            }
    }
}