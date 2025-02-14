package heckerpowered.matrix

import heckerpowered.matrix.common.MagicManager
import heckerpowered.matrix.common.MatrixServerPlayNetworking
import heckerpowered.matrix.common.command.MatrixCommands
import heckerpowered.matrix.common.effect.MatrixStatusEffects
import heckerpowered.matrix.common.enchantment.MatrixEnchantments
import heckerpowered.matrix.common.item.MatrixComponents
import heckerpowered.matrix.common.item.MatrixItemGroups
import heckerpowered.matrix.common.item.MatrixItems
import heckerpowered.matrix.common.item.MatrixPotions
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.recipe.MatrixRecipeSerializer
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import kotlin.math.exp

object Matrix : ModInitializer {
    const val MOD_ID = "matrix"
    private val logger = LoggerFactory.getLogger("matrix")

    override fun onInitialize() {
        MatrixServerPlayNetworking.onInitialize()
        MagicManager.onInitialize()
        MatrixStatusEffects.onInitialize()
        MatrixEnchantments.onInitialize()
        MatrixCommands.onInitialize()
        MatrixComponents.onInitialize()
        MatrixItems.onInitialize()
        MatrixItemGroups.onInitialize()
        MatrixRecipeSerializer.onInitialize()
        MatrixPotions.onInitialize()
        ChannelSequence.onInitialize()
    }

    fun identifier(path: String): Identifier {
        return Identifier.of("matrix", path)
    }

    private fun generateGaussianKernel(kernelSize: Int, sigma: Float): List<Float> {
        val kernel = mutableListOf<Float>()
        var sum = 0F

        for (i in 0..<kernelSize) {
            val x = i - kernelSize / 2
            val result = exp(-0.5F * (x * x) / (sigma * sigma))
            kernel.add(result)
            sum += result
        }

        for (i in 0..<kernelSize) {
            kernel[i] /= sum
        }

        return kernel
    }
}