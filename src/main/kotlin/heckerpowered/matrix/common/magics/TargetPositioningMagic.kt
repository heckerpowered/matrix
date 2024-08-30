package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

class TargetPositioningMagic : Magic(Text.literal("目标定位"), 2) {
    override fun onUse(player: PlayerEntity, target: Entity) {
        target.world.getOtherEntities(player, target.boundingBox.expand(32.0)).forEach {
            if (it is LivingEntity) {
                it.addStatusEffect(StatusEffectInstance(StatusEffects.GLOWING, 200, 0, true, false))
            }
        }
    }
}