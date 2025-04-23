package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.event.GetArmorCallback
import heckerpowered.matrix.common.event.GetAttributeValueCallback
import heckerpowered.matrix.core.Accumulator
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.registry.entry.RegistryEntry

object ArmorPenetrationEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    0xFF4500
) {
    init {
        GetArmorCallback.EVENT.register(::getArmor)
        GetAttributeValueCallback.EVENT.register(::getAttributeValue)
    }

    private fun getAttributeValue(entity: LivingEntity, attribute: RegistryEntry<EntityAttribute>, accumulator: Accumulator) {
        if (attribute != EntityAttributes.GENERIC_ARMOR_TOUGHNESS) {
            return
        }

        val armorPenetrationInstance = entity.getStatusEffect(armorPenetrationEffect) ?: return
        val amplifier = armorPenetrationInstance.amplifier + 1
        accumulator.multiplier -= amplifier * 0.4
    }

    private fun getArmor(entity: LivingEntity, accumulator: Accumulator) {
        val armorPenetrationInstance = entity.getStatusEffect(armorPenetrationEffect) ?: return
        val amplifier = armorPenetrationInstance.amplifier + 1
        accumulator.multiplier -= amplifier * 0.4
    }
}