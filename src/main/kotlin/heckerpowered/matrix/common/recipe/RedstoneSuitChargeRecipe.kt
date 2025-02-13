package heckerpowered.matrix.common.recipe

import heckerpowered.matrix.common.item.isRedstoneSuit
import heckerpowered.matrix.common.item.redstoneSuitMaxPower
import heckerpowered.matrix.common.item.redstoneSuitPower
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.input.CraftingRecipeInput
import net.minecraft.registry.RegistryWrapper
import net.minecraft.world.World

class RedstoneSuitChargeRecipe(craftingRecipeCategory: CraftingRecipeCategory) :
    SpecialCraftingRecipe(craftingRecipeCategory) {

    private data class RedstoneCharger(val redstoneSuit: ItemStack, val redstone: ItemStack)

    private fun findCharger(input: CraftingRecipeInput): RedstoneCharger? {
        var redstoneSuit: ItemStack? = null
        var redstone: ItemStack? = null

        for (stack in input.stacks) {
            if (stack.isRedstoneSuit()) {
                redstoneSuit = stack
            } else if (stack.item == Items.REDSTONE || stack.item == Items.REDSTONE_BLOCK) {
                redstone = stack
            }
            if (redstoneSuit != null && redstone != null) {
                return RedstoneCharger(redstoneSuit, redstone)
            }
        }

        return null
    }

    override fun matches(input: CraftingRecipeInput, world: World): Boolean {
        val charger = findCharger(input) ?: return false
        return charger.redstoneSuit.redstoneSuitPower < charger.redstoneSuit.redstoneSuitMaxPower
    }

    override fun craft(input: CraftingRecipeInput, lookup: RegistryWrapper.WrapperLookup): ItemStack {
        val charger = findCharger(input) ?: return ItemStack.EMPTY
        val output = charger.redstoneSuit.copy()

        output.redstoneSuitPower += when (charger.redstone.item) {
            Items.REDSTONE -> 1
            Items.REDSTONE_BLOCK -> 9
            else -> 0
        }
        return output
    }

    override fun fits(width: Int, height: Int): Boolean {
        return width * height >= 2
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return MatrixRecipeSerializer.redstoneSuitChargerRecipeSerializer
    }
}