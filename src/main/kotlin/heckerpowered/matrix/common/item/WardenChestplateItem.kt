package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.*
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting

object WardenChestplateItem : ArmorItem(
    wardenArmorMaterial,
    Type.CHESTPLATE,
    Settings()
        .fireproof()
        .maxDamage(Type.CHESTPLATE.getMaxDamage(37))
) {
    init {
        LivingAttackCallback.event.register(::onLivingAttack)
        LivingHurtCallback.event.register(::onLivingHurt)
        LivingKnockbackCallback.event.register(::onLivingKnockback)
    }

    private fun onLivingAttack(event: DamageAccumulator): ActionResult {
        if (isAngered(event.attacker!!)) {
            event.damageMultiplier += 1
        }
        return ActionResult.PASS
    }

    private fun onLivingHurt(event: DamageAccumulator): ActionResult {
        if (isAngered(event.target)) {
            event.immune = true
        }
        return ActionResult.PASS
    }

    private fun onLivingKnockback(event: LivingKnockbackEvent): ActionResult {
        if (isAngered(event.entity)) {
            return ActionResult.FAIL
        }
        return ActionResult.PASS
    }

    override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
        super.appendTooltip(stack, context, tooltip, type)
        val lines = MatrixLanguage.wardenChestplateDescription.string.split('\n').map {
            Text.literal(it).formatted(Formatting.GRAY, Formatting.ITALIC)
        }
        tooltip.addAll(lines)
    }

    @JvmStatic
    fun isAngered(entity: LivingEntity): Boolean {
        if (entity.getEquippedStack(EquipmentSlot.CHEST).item != this) {
            return false
        }

        return isWardenArmorAngered(entity)
    }
}