package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.event.GetArmorCallback
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory

object IgniteEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    0xD9471D
) {
    init {
        GetArmorCallback.EVENT
    }
}