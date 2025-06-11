package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.LivingDamageCallback
import heckerpowered.matrix.common.event.LivingDamageEvent
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_MAX_POWER
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_POWER
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.tag.ItemTags
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting

object RedstoneChestplateItem : ArmorItem(
    redstoneArmorMaterial,
    Type.CHESTPLATE,
    Settings()
        .maxDamage(Type.CHESTPLATE.getMaxDamage(24))
        .component(REDSTONE_SUIT_MAX_POWER, 20)
        .component(REDSTONE_SUIT_POWER, 0)
), RedstoneSuit {
    init {
        ItemTags.ARMOR_ENCHANTABLE
        LivingDamageCallback.EVENT.register(this::onLivingDamage)
    }

    private fun onLivingDamage(event: LivingDamageEvent): ActionResult {
        val entity = event.entity
        val chestplate = entity.getEquippedStack(EquipmentSlot.CHEST)
        if (!chestplate.isRedstoneSuit() || chestplate.redstoneSuitPower <= 0) {
            return ActionResult.PASS
        }

        val damageToReduce = (event.amount * 0.4).coerceAtMost(chestplate.redstoneSuitPower * 4.0)
        val powerUsage = damageToReduce / 4
        chestplate.redstoneSuitPower -= powerUsage.toLong()
        event.amount -= damageToReduce.toFloat()

        return ActionResult.CONSUME
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType,
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        RedstoneSuit.appendTooltip(stack, context, tooltip, type)
        tooltip.add(MatrixLanguage.redstoneChestplateDescription.copy().formatted(Formatting.GRAY, Formatting.ITALIC))
    }
}