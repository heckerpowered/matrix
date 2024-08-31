package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

class TargetPositioningMagic : Magic(MatrixLanguage.magicTargetPositioning, 2) {
    override fun onUse(player: PlayerEntity, target: Entity) {
        target.world.getOtherEntities(player, target.boundingBox.expand(32.0)).forEach {
            if (it is LivingEntity) {
                it.addStatusEffect(StatusEffectInstance(StatusEffects.GLOWING, 200, 0, true, false))
            }
        }
    }

    override fun getDescription(): List<Text> {
        return listOf(
            MatrixLanguage.magicTargetPositioningDescription1,
            MatrixLanguage.magicTargetPositioningDescription2,
        )
    }
}