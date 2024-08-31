package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

class HealthStealMagic : Magic(
    MatrixLanguage.magicHealthSteal,
    20
) {
    override fun onUse(player: PlayerEntity, target: Entity) {
        if (target !is LivingEntity) {
            return
        }

        player.absorptionAmount += target.health
    }

    override fun getDescription(): List<Text> {
        return listOf(
            MatrixLanguage.magicHealthStealDescription1,
            MatrixLanguage.magicHealthStealDescription2,
            MatrixLanguage.magicHealthStealDescription3,
        )
    }
}