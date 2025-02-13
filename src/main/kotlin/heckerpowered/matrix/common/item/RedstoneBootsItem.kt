package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text

object RedstoneBootsItem : ArmorItem(
    redstoneArmorMaterial,
    Type.BOOTS,
    Settings()
        .maxDamage(Type.BOOTS.getMaxDamage(24))
        .component(redstoneSuitMaxPowerComponent, 20)
        .component(redstoneSuitPowerComponent, 0)
), RedstoneSuit {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType,
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        RedstoneSuit.appendTooltip(stack, context, tooltip, type)
    }
}