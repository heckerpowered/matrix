package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity

object TargetPositioningMagic : Magic(MatrixLanguage.magicTargetPositioning, 4, MatrixLanguage.magicTargetPositioningDescription, 20) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        super.cast(player, target, sequence, data)
        target.world.getOtherEntities(player, target.boundingBox.expand(24.0)).forEach {
            if (it is LivingEntity) {
                it.addStatusEffect(StatusEffectInstance(StatusEffects.GLOWING, 200, 0, true, false))
            }
        }
    }
}