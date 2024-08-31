package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

class DecisiveStrikeMagic : Magic(MatrixLanguage.magicDecisiveStrike, 6) {
    override fun onUse(player: PlayerEntity, target: Entity) {
        target.timeUntilRegen = 0
        target.damage(target.damageSources.playerAttack(player), 6.0f)
    }

    override fun getDescription(): List<Text> {
        return listOf(
            MatrixLanguage.magicDecisiveStrikeDescription1,
            MatrixLanguage.magicDecisiveStrikeDescription2,
            MatrixLanguage.magicDecisiveStrikeDescription3,


            )
    }
}