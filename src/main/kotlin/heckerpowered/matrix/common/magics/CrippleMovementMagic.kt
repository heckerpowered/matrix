package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.effect.crippleMovementEffect
import heckerpowered.matrix.common.isInvulnerableToEffect
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld

object CrippleMovementMagic : Magic(MatrixLanguage.magicCrippleMovement, 6, MatrixLanguage.magicCrippleMovementDescription, 2) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        if (target is PlayerEntity) {
            target.addStatusEffect(StatusEffectInstance(crippleMovementEffect, 20 * 3, 0))
            return
        }
        target.addStatusEffect(StatusEffectInstance(crippleMovementEffect, 20 * 10, 0))
        if (target.world !is ServerWorld) {
            return
        }

        val server = target.world.server ?: return
        val statusEffectInstance = target.getStatusEffect(crippleMovementEffect) ?: return
        for (serverPlayer in server.playerManager.playerList) {
            serverPlayer.networkHandler.sendPacket(EntityStatusEffectS2CPacket(target.id, statusEffectInstance, false))
        }
    }

    override fun availableStatus(
        player: PlayerEntity,
        target: LivingEntity?,
        sequence: ChannelSequence?,
    ): MagicAvailableStatus {
        if (target?.isInvulnerableToEffect(crippleMovementEffect) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }

    override fun getCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): Long {
        if (target is PlayerEntity) {
            return super.getCost(player, target, sequence) * 3
        }

        return super.getCost(player, target, sequence)
    }
}