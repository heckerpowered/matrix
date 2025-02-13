package heckerpowered.matrix.common.effect

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects

object AngeredEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0xFF4500
) {
    override fun onApplied(entity: LivingEntity?, amplifier: Int) {
        super.onApplied(entity, amplifier)
        if (entity == null) {
            return
        }

        entity.activeStatusEffects
            .filter { !it.value.effectType.value().isBeneficial }
            .map { it.key }
            .forEach { entity.removeStatusEffect(it) }
        val angeredEffectInstance = entity.getStatusEffect(angeredEffect) ?: return
        entity.addStatusEffect(StatusEffectInstance(StatusEffects.DARKNESS, angeredEffectInstance.duration, 0))
    }
}