package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.event.EntityTickCallback
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.sound.SoundEvents

object AngeredEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0xFF4500
) {
    init {
        EntityTickCallback.event.register(::onEntityTick)
    }

    private fun onEntityTick(entity: LivingEntity) {
        if (entity.hasStatusEffect(angeredEffect) && entity.age % 10 == 0) {
            entity.apply {
                world.playSound(x, y, z, SoundEvents.ENTITY_WARDEN_HEARTBEAT, soundCategory, 5.0F, soundPitch, false)
            }
        }
    }

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