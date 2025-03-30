package heckerpowered.matrix.common

import heckerpowered.matrix.common.enchantment.getEnchantmentLevel
import heckerpowered.matrix.common.enchantment.manaOverflow
import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.magics.*
import heckerpowered.matrix.common.network.SyncManaPayload
import heckerpowered.matrix.common.persistent.isInfiniteMana
import heckerpowered.matrix.common.persistent.mana
import heckerpowered.matrix.common.persistent.maxMana
import heckerpowered.matrix.common.persistent.wizardHelmet
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity

object MagicManager {
    private val magics = mutableMapOf<Int, Magic>()

    init {
        registerMagics()
    }

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
        registerMagic(PullMagic)
        registerMagic(LevitationMagic)
    }

    fun getMagics(player: PlayerEntity): List<Magic> {
        val itemStack = player.getEquippedStack(EquipmentSlot.HEAD)
        val item = itemStack.item
        if (item !is WizardHelmet) {
            return listOf()
        }

        return item.getMagics(player, itemStack)
    }

    fun onInitialize() {
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
                val wizardHelmet = it.wizardHelmet
                val item = wizardHelmet.item
                if (item is WizardHelmet) {
                    it.maxMana = item.getMaxMana(it, wizardHelmet)
                } else {
                    it.maxMana = .0
                }

                var manaRegen = 1.0
                val manaOverflowLevel = wizardHelmet.getEnchantmentLevel(manaOverflow)
                if (manaOverflowLevel > 0) {
                    manaRegen += manaRegen * (manaOverflowLevel * 0.3)
                }
                it.mana += manaRegen
                ServerPlayNetworking.send(it, SyncManaPayload(it.mana, it.maxMana))
            }
        }
    }
}