package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

class HurtMagic : Magic(Text.literal("Hurt"), 6) {
    override fun onUse(player: PlayerEntity, target: Entity) {
        target.timeUntilRegen = 0
        target.damage(target.damageSources.playerAttack(player), 20.0f)
    }
}