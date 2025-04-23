package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.ActionResult

object BrutalStrengthEnchantment {
    fun onInitialize() {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
    }

    private fun onLivingAttack(accumulator: DamageAccumulator): ActionResult {
        val attacker = accumulator.attacker!!
        val target = accumulator.target

        val registryManager = attacker.world.registryManager
        val registryWrapper = registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
        val enchantmentEntry = registryWrapper.getOrThrow(brutalStrength)
        val equippedHelmet = attacker.getEquippedStack(EquipmentSlot.HEAD)
        val enchantmentLevel = EnchantmentHelper.getLevel(enchantmentEntry, equippedHelmet)
        if (enchantmentLevel <= 0) {
            return ActionResult.PASS
        }

        if (attacker.attacking != target &&
            accumulator.damageSource.isOf(MatrixDamageTypes.magic)
        ) {
            accumulator.damageMultiplier += enchantmentLevel * 0.08
            attacker.onAttacking(target)
        }
        return ActionResult.PASS
    }
}