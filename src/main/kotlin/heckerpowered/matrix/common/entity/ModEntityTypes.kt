package heckerpowered.matrix.common.entity

import heckerpowered.matrix.Matrix
import net.fabricmc.api.ModInitializer
import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import org.slf4j.LoggerFactory

object ModEntities : ModInitializer {
    private val logger = LoggerFactory.getLogger("item")

    private fun <T : EntityType<T>> registerEntityType(name: String, entityType: T) =
        Registry.register(Registries.ENTITY_TYPE, Matrix.identifier(name), entityType)

    override fun onInitialize() {
        logger.debug(Matrix.LOGGER_MARKER, "Initializing entities")
    }
}