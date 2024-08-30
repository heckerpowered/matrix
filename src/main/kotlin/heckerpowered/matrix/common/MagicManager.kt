package heckerpowered.matrix.common

import heckerpowered.matrix.common.magics.HurtMagic
import heckerpowered.matrix.common.magics.TargetPositioningMagic
import heckerpowered.matrix.common.persistent.ManaState
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.player.PlayerEntity

object MagicManager {
    fun getMagic(player: PlayerEntity, index: Int): Magic? {
        if (index == 1) {
            return TargetPositioningMagic()
        }
        return HurtMagic()
    }

    fun onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register {
            if (it.ticks % 5 != 0) {
                return@register
            }

            it.playerManager.playerList.forEach {
                ++ManaState.getPlayerState(it).mana
            }
        }
    }
}