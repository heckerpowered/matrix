package heckerpowered.matrix.common.item

import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.PickaxeItem
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object RedstonePickaxeItem : PickaxeItem(
    redstoneToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(redstoneToolMaterial, 1.0F, -2.8F))
        .component(redstoneSuitMaxPowerComponent, 20)
        .component(redstoneSuitPowerComponent, 0)
), RedstoneSuit {
    override fun getMiningSpeed(stack: ItemStack, state: BlockState): Float {
        val miningSpeed = super.getMiningSpeed(stack, state)
        if (stack.redstoneSuitPower > 0) {
            return miningSpeed * 1.4F
        }
        return miningSpeed
    }

    override fun postMine(
        stack: ItemStack,
        world: World,
        state: BlockState,
        pos: BlockPos,
        miner: LivingEntity
    ): Boolean {
        --stack.redstoneSuitPower
        return super.postMine(stack, world, state, pos, miner)
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        RedstoneSuit.appendTooltip(stack, context, tooltip, type)
        tooltip.add(MatrixLanguage.redstoneMiningToolDescription.copy().formatted(Formatting.GRAY, Formatting.ITALIC))
    }
}