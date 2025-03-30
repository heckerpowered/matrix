package heckerpowered.matrix.common.effect

import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.player.PlayerEntity

val PlayerEntity.bloodPactActive: Boolean
    get() = hasStatusEffect(bloodPactEffect)

object BloodPactEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0xFF0000
)