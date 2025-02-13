package heckerpowered.matrix.common.item

import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Formatting

interface RedstoneSuit {
    companion object {
        fun appendTooltip(
            stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType
        ) {
            if (!stack.isRedstoneSuit()) {
                return
            }

            tooltip.add(
                MatrixLanguage.redstoneSuitPower.copy()
                    .append("${stack.redstoneSuitPower}/${stack.redstoneSuitMaxPower}")
                    .formatted(Formatting.GRAY, Formatting.ITALIC)
            )
        }
    }
}

fun ItemStack.isRedstoneSuit(): Boolean {
    return item is RedstoneSuit
}

var ItemStack.redstoneSuitMaxPower: Long
    get() = getOrDefault(redstoneSuitMaxPowerComponent, 0).coerceAtLeast(0)
    set(value) {
        set(redstoneSuitMaxPowerComponent, value.coerceAtLeast(0))
    }

var ItemStack.redstoneSuitPower: Long
    get() = getOrDefault(redstoneSuitPowerComponent, 0).coerceAtLeast(0)
    set(value) {
        set(redstoneSuitPowerComponent, value.coerceIn(0..redstoneSuitMaxPower))
    }