package heckerpowered.matrix.common

import heckerpowered.matrix.common.magics.DecisiveStrikeMagic
import heckerpowered.matrix.common.magics.HealthStealMagic
import heckerpowered.matrix.common.magics.ManaOverloadMagic
import heckerpowered.matrix.common.magics.TargetPositioningMagic
import heckerpowered.matrix.common.network.SyncManaPayload
import heckerpowered.matrix.common.persistent.ManaState
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity

object MagicManager {
    fun getMagic(player: PlayerEntity, index: Int): Magic? {
        return when (index) {
            1 -> TargetPositioningMagic()
            2 -> DecisiveStrikeMagic()
            3 -> HealthStealMagic()
            4 -> ManaOverloadMagic()
            else -> null
        }
    }

    fun onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register { it ->
            if (it.ticks % 20 != 0) {
                return@register
            }

            it.playerManager.playerList.forEach {
                val manaState = ManaState.getPlayerState(it)
                manaState.mana += 5
                if (manaState.mana > manaState.maxMana) {
                    manaState.mana = manaState.maxMana
                }
                ServerPlayNetworking.send(it, SyncManaPayload(manaState.mana, manaState.maxMana))
            }
        }
    }
}