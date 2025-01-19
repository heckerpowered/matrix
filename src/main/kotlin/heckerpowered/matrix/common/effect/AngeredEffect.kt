package heckerpowered.matrix.common.effect

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory

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
    }
}