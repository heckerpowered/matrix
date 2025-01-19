package heckerpowered.matrix.common.item

import heckerpowered.matrix.Matrix
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object MatrixItems {
    private fun register(item: Item, name: String): Item {
        val identifier = Matrix.identifier(name)
        val registeredItem = Registry.register(Registries.ITEM, identifier, item)
        return registeredItem
    }

    fun onInitialize() {
        register(WardenChestplateItem, "warden_chestplate")
    }
}