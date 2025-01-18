package heckerpowered.matrix.common.effect

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory

object WitherArmorEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0x5A5A5A
) {
    override fun onApplied(entity: LivingEntity, amplifier: Int) {
        super.onApplied(entity, amplifier)
        val newAbsorptionAmount = entity.maxHealth * (0.05F + (amplifier + 1) * 0.05F)
        val maxAbsorptionAmount = entity.maxHealth * 0.5F
        if (entity.absorptionAmount >= maxAbsorptionAmount) {
            return
        }
        if (entity.absorptionAmount + newAbsorptionAmount >= maxAbsorptionAmount) {
            entity.absorptionAmount = maxAbsorptionAmount
            return
        }

        entity.absorptionAmount += newAbsorptionAmount
    }
}