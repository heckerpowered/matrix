package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.ActionResult

object GuaranteedEnchantment {
    fun onInitialize() {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
    }

    private fun onLivingAttack(event: DamageAccumulator): ActionResult {
        if (event.attacker!!.world.isClient) {
            return ActionResult.PASS
        }

        val attacker = event.attacker
        val target = event.target
        val guaranteedEnchantmentEntry = attacker.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(guaranteedEnchantmentKey)
        val guaranteedEnchantmentLevel = EnchantmentHelper.getEquipmentLevel(guaranteedEnchantmentEntry, attacker)
        if (guaranteedEnchantmentLevel <= 0) {
            return ActionResult.PASS
        }

        val percentage = target.maxHealth.toDouble() / attacker.maxHealth.toDouble()
        if (percentage <= 1) {
            return ActionResult.PASS
        }

        val damageBonusRatio = (percentage - 1).coerceAtMost(0.3) * guaranteedEnchantmentLevel
        event.damageMultiplier += damageBonusRatio
        return ActionResult.PASS
    }
}