package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.effect.angeredEffect
import heckerpowered.matrix.common.event.LivingHurtCallback
import heckerpowered.matrix.common.event.LivingHurtEvent
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.util.ActionResult

object WardenChestplateItem : ArmorItem(
    wardenArmorMaterial,
    Type.CHESTPLATE,
    Settings().apply {
        fireproof()
        maxDamage(Type.CHESTPLATE.getMaxDamage(37))
    }
) {
    init {
        LivingHurtCallback.event.register(::onLivingHurt)
        LivingHurtCallback.event.register(::onLivingKnockback)
    }

    private fun onLivingHurt(event: LivingHurtEvent): ActionResult {
        if (isAngered(event.entity)) {
            event.amount = .0F
        }

        event.damageSource.attacker?.let { it as? LivingEntity? }?.let { attacker ->
            if (isAngered(attacker)) {
                event.amount *= 2
            }
        }

        return ActionResult.PASS
    }

    private fun onLivingKnockback(event: LivingHurtEvent): ActionResult {
        if (isAngered(event.entity)) {
            return ActionResult.FAIL
        }
        return ActionResult.PASS
    }

    @JvmStatic
    fun isAngered(entity: LivingEntity): Boolean {
        if (entity.getEquippedStack(EquipmentSlot.CHEST).item != this) {
            return false
        }

        entity.getStatusEffect(angeredEffect) ?: return false
        return true
    }
}