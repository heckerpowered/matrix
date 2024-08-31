package heckerpowered.matrix.common

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

open class Magic(
    val text: Text,
    val cost: Int
) {
    open fun onUse(player: PlayerEntity, target: Entity) {
    }

    open fun getDescription(): List<Text> {
        return listOf()
    }
}