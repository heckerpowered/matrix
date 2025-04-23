package heckerpowered.matrix.common.effect

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.event.EntityTickCallback
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.sound.SoundEvents

object AngeredEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0xFF4500
) {
    init {
        EntityTickCallback.EVENT.register(::onEntityTick)

        addAttributeModifier(
            EntityAttributes.GENERIC_MOVEMENT_SPEED, Matrix.identifier("angered"), 0.2, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        )
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
    }
}