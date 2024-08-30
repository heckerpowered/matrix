package heckerpowered.matrix

import heckerpowered.matrix.common.MagicManager
import heckerpowered.matrix.common.MatrixServerPlayNetworking
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object Matrix : ModInitializer {
    const val MOD_ID = "matrix"
    private val logger = LoggerFactory.getLogger("matrix")

    override fun onInitialize() {
        MatrixServerPlayNetworking.onInitialize()
        MagicManager.onInitialize()
    }

    fun identifier(path: String): Identifier {
        return Identifier.of("matrix", path)
    }
}