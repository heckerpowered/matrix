package heckerpowered.matrix.common

import heckerpowered.matrix.common.magics.*
import heckerpowered.matrix.common.network.SyncManaPayload
import heckerpowered.matrix.common.persistent.ManaState
import heckerpowered.matrix.common.persistent.isInfiniteMana
import heckerpowered.matrix.common.persistent.mana
import heckerpowered.matrix.common.persistent.maxMana
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object MagicManager {
    private val magics = mutableMapOf<Int, Magic>()

    fun getRegisteredMagics(): List<Magic> {
        return magics.values.toList()
    }

    fun getMagicByName(name: String): Magic? {
        return magics[name.hashCode()]
    }

    fun getMagicById(id: Int): Magic? {
        return magics[id]
    }

    fun registerMagic(magic: Magic) {
        magics[magic.name.hashCode()] = magic
    }

    private fun registerMagics() {
        registerMagic(TargetPositioningMagic)
        registerMagic(DecisiveStrikeMagic)
        registerMagic(HealthStealMagic)
        registerMagic(ManaOverloadMagic)
        registerMagic(ExplosionMagic)
        registerMagic(KillMagic)
        registerMagic(SculkCatalystMagic)
        registerMagic(IgniteMagic)
        registerMagic(BreakingBadMagic)
        registerMagic(CrippleMovementMagic)
        registerMagic(MemoryEraseMagic)
        registerMagic(SpreadMagic)
        registerMagic(SystemCrashMagic)
        registerMagic(LightningBoltMagic)
        registerMagic(TeleportMagic)
        registerMagic(ArmorPenetrationMagic)
        registerMagic(SonicBoomMagic)
        registerMagic(BruteForceMagic)
    }

    fun onInitialize() {
        registerMagics()
        ServerTickEvents.END_SERVER_TICK.register { it ->
            it.playerManager.playerList.forEach {
                if (it.isInfiniteMana) {
                    it.mana = it.maxMana
                }
            }
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