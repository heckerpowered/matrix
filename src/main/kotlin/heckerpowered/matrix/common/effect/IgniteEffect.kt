package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.effect.MatrixStatusEffects.IGNITE_EFFECT
import heckerpowered.matrix.common.event.AccumulateAttributeValueCallback
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.GetArmorCallback
import heckerpowered.matrix.common.event.LivingHurtCallback
import heckerpowered.matrix.core.Accumulator
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.ActionResult

object IgniteEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    0xD9471D
) {
    init {
        GetArmorCallback.EVENT.register(::getArmor)
        AccumulateAttributeValueCallback.EVENT.register(::getAttributeValue)
        LivingHurtCallback.EVENT.register(::onLivingHurt)
    }

    private fun getAttributeValue(entity: LivingEntity, attribute: RegistryEntry<EntityAttribute>, accumulator: Accumulator) {
        if (attribute == EntityAttributes.GENERIC_ARMOR_TOUGHNESS && entity.hasStatusEffect(IGNITE_EFFECT)) {
            accumulator.multiplier -= 0.4
        }
    }

    private fun getArmor(entity: LivingEntity, accumulator: Accumulator) {
        if (entity.hasStatusEffect(IGNITE_EFFECT)) {
            accumulator.multiplier -= 0.4
        }
    }

    private fun onLivingHurt(event: DamageAccumulator): ActionResult {
        val target = event.target
        val igniteEffect = target.getStatusEffect(IGNITE_EFFECT)
        if (igniteEffect != null) {
            event.damageMultiplier += 0.2
            igniteEffect.mapDuration { it + 10 }
            target.fireTicks += 10
        }

        return ActionResult.PASS
    }
}