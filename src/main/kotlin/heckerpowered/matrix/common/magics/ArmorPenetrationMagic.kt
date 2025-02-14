package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.effect.armorPenetrationEffect
import heckerpowered.matrix.common.isInvulnerableToEffect
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object ArmorPenetrationMagic : Magic(MatrixLanguage.magicArmorPenetrationMagic, 8, MatrixLanguage.magicArmorPenetrationMagicDescription, 8) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        target.addStatusEffect(StatusEffectInstance(armorPenetrationEffect, 200, 1, false, false))
    }

    override fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): MagicAvailableStatus {
        if (target?.isInvulnerableToEffect(armorPenetrationEffect) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}