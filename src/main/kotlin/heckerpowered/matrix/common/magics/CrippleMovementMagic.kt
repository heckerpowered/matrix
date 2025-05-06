package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.effect.MatrixStatusEffects.CRIPPLE_MOVEMENT_EFFECT
import heckerpowered.matrix.common.isInvulnerableToEffect
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld

object CrippleMovementMagic : Magic(MatrixLanguage.magicCrippleMovement, 6, MatrixLanguage.magicCrippleMovementDescription, 6) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        super.cast(player, target, sequence, data)
        if (target is PlayerEntity) {
            target.addStatusEffect(StatusEffectInstance(CRIPPLE_MOVEMENT_EFFECT, 20 * 3, 0))
            return
        }
        target.addStatusEffect(StatusEffectInstance(CRIPPLE_MOVEMENT_EFFECT, 20 * 10, 0))
        if (target.world !is ServerWorld) {
            return
        }

        val server = target.world.server ?: return
        val statusEffectInstance = target.getStatusEffect(CRIPPLE_MOVEMENT_EFFECT) ?: return
        for (serverPlayer in server.playerManager.playerList) {
            serverPlayer.networkHandler.sendPacket(EntityStatusEffectS2CPacket(target.id, statusEffectInstance, false))
        }
    }

    override fun availableStatus(
        player: PlayerEntity,
        target: LivingEntity?,
        sequence: ChannelSequence?,
    ): MagicAvailableStatus {
        if (target?.isInvulnerableToEffect(CRIPPLE_MOVEMENT_EFFECT) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }

    override fun getCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?, data: MagicData): Long {
        if (target is PlayerEntity) {
            return super.getCost(player, target, sequence, data) * 3
        }

        return super.getCost(player, target, sequence, data)
    }
}