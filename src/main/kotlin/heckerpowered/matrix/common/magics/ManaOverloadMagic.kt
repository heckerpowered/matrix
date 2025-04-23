package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.effect.MatrixStatusEffects.MANA_OVERLOAD_EFFECT
import heckerpowered.matrix.common.isInvulnerableToEffect
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket
import net.minecraft.server.network.ServerPlayerEntity

object ManaOverloadMagic : Magic(MatrixLanguage.magicManaOverload, 10, MatrixLanguage.magicManaOverloadDescription, 10) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        val manaOverloadInstance = target.getStatusEffect(MANA_OVERLOAD_EFFECT)

        val nextAmplifier = (manaOverloadInstance?.amplifier ?: -1) + 1
        target.addStatusEffect(
            StatusEffectInstance(
                MANA_OVERLOAD_EFFECT,
                20 * 10,
                nextAmplifier.coerceAtMost(7),
                true, false
            )
        )
        when (nextAmplifier) {
            1 -> {
                target.statusEffects
                    .filter { it.effectType.value().isBeneficial }
                    .map { it.effectType }
                    .forEach { target.removeStatusEffect(it) }
            }

            7 -> {
                target.damage(target.damageSources.create(MatrixDamageTypes.magic), target.health)
            }
        }
        val server = target.world.server ?: return
        val statusEffectInstance = target.getStatusEffect(MANA_OVERLOAD_EFFECT) ?: return
        for (serverPlayer in server.playerManager.playerList) {
            serverPlayer.networkHandler.sendPacket(EntityStatusEffectS2CPacket(target.id, statusEffectInstance, false))
        }
    }

    override fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): MagicAvailableStatus {
        val effect = target?.getStatusEffect(MANA_OVERLOAD_EFFECT)
        if (target?.isInvulnerableToEffect(MANA_OVERLOAD_EFFECT) == true ||
            (effect?.amplifier ?: 0) >= 7 && (effect?.duration != 0)
        ) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}