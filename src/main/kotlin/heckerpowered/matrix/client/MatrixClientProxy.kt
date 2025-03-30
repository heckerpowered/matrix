package heckerpowered.matrix.client

import heckerpowered.matrix.common.MatrixCommonProxy
import net.minecraft.entity.player.PlayerEntity

class MatrixClientProxy : MatrixCommonProxy() {
    override fun getPlayerMana(player: PlayerEntity): Double {
        if (minecraft.player == null || player != ::player.get()) {
            return .0
        }

        return MatrixHud.mana - MatrixHud.manaUsage
    }

    override fun getPlayerMaxMana(player: PlayerEntity): Double {
        if (minecraft.player == null || player != ::player.get()) {
            return .0
        }

        return MatrixHud.maxMana
    }
}