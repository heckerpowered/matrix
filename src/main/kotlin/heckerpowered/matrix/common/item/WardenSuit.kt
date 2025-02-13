package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.effect.angeredEffect
import net.minecraft.entity.LivingEntity

fun isWardenArmorAngered(entity: LivingEntity): Boolean {
    return entity.getStatusEffect(angeredEffect) != null
}