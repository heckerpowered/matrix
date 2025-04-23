package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity

object LevitationMagic : Magic(MatrixLanguage.magicLevitation, 30, MatrixLanguage.magicLevitationDescription, 40) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        val statusEffectInstance = target.getStatusEffect(StatusEffects.LEVITATION)
        val amplifier = statusEffectInstance?.amplifier ?: -1
        target.addStatusEffect(StatusEffectInstance(StatusEffects.LEVITATION, 20 * 10, amplifier + 1))
    }
}