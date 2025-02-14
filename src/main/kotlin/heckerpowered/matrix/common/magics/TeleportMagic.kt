package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity

object TeleportMagic : Magic(MatrixLanguage.magicTeleport, 15, MatrixLanguage.magicTeleportDescription, 5) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        if (player == null) {
            return
        }

        player.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, 200, 0, false, false))
        player.addStatusEffect(StatusEffectInstance(StatusEffects.SPEED, 200, 4, false, false))
        val velocity = player.velocity
        player.teleport(target.x, target.y, target.z, true)
        player.networkHandler.sendPacket(EntityVelocityUpdateS2CPacket(player))
        player.velocity = velocity
        player.velocityDirty = true
        player.velocityModified = true
    }
}