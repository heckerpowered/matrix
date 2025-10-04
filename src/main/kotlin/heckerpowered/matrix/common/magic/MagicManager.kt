/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.common.enchantment.MatrixEnchantments
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.getEnchantmentLevel
import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.network.SyncManaPayload
import heckerpowered.matrix.common.persistent.isInfiniteMana
import heckerpowered.matrix.common.persistent.mana
import heckerpowered.matrix.common.persistent.maxMana
import heckerpowered.matrix.common.persistent.wizardHelmet
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity
import java.util.*

object MagicManager {
    private val magics = mutableMapOf<UUID, Magic>()
    private var registeredMagics = mutableListOf<Magic>()
    private var snapshot = Collections.unmodifiableList(registeredMagics)

    fun getRegisteredMagics(): List<Magic> = snapshot

    fun getMagicByUuid(id: UUID): Magic? = magics[id]

    fun registerMagic(magic: Magic) {
        val uuid = magic.definition.uuid
        require(magics.putIfAbsent(uuid, magic) == null) { "Duplicate Magic: $uuid" }
        registeredMagics.add(magic)
    }

    private fun registerBuiltinMagics() {
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
        registerMagic(AttractMagic)
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
        registerBuiltinMagics()

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
                val manaRegenerationLevel = wizardHelmet.getEnchantmentLevel(MatrixEnchantments.MANA_REGENERATION_ENCHANTMENT_KEY)
                if (manaRegenerationLevel > 0) {
                    manaRegen += manaRegen * (manaRegenerationLevel * 0.3)
                }
                it.mana += manaRegen
                ServerPlayNetworking.send(it, SyncManaPayload(it.mana, it.maxMana))
            }
        }
    }
}