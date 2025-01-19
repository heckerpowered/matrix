package heckerpowered.matrix

import heckerpowered.matrix.common.MagicManager
import heckerpowered.matrix.common.MatrixServerPlayNetworking
import heckerpowered.matrix.common.command.MatrixCommands
import heckerpowered.matrix.common.effect.MatrixStatusEffects
import heckerpowered.matrix.common.enchantment.MatrixEnchantments
import heckerpowered.matrix.common.item.MatrixItems
import heckerpowered.matrix.common.persistent.ChannelSequence
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object Matrix : ModInitializer {
    const val MOD_ID = "matrix"
    private val logger = LoggerFactory.getLogger("matrix")

    override fun onInitialize() {
        MatrixServerPlayNetworking.onInitialize()
        MagicManager.onInitialize()
        MatrixStatusEffects.onInitialize()
        MatrixEnchantments.onInitialize()
        MatrixCommands.onInitialize()
        MatrixItems.onInitialize()
        ChannelSequence.onInitialize()
    }

    fun identifier(path: String): Identifier {
        return Identifier.of("matrix", path)
    }
}