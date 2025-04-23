package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.effect.MatrixStatusEffects.ANGERED_EFFECT
import net.minecraft.entity.LivingEntity

fun isWardenArmorAngered(entity: LivingEntity): Boolean {
    return entity.getStatusEffect(ANGERED_EFFECT) != null
}