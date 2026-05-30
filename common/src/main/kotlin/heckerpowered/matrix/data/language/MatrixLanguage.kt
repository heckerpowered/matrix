/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.data.language

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.Enchantment

val MutableComponent.key: String
    get() {
        return (contents as TranslatableContents).key
    }

fun TranslationBuilder.add(enchantment: ResourceKey<Enchantment>, value: String) {
    addEnchantment(enchantment, value)
}

object MatrixLanguage {
    val mana: MutableComponent = Component.translatable("matrix.mana")

    val magicTargetPositioning: MutableComponent = Component.translatable("matrix.target_positioning")
    val magicTargetPositioningDescription: MutableComponent = Component.translatable("matrix.target_positioning.description")

    val magicDecisiveStrike: MutableComponent = Component.translatable("matrix.decisive_strike")
    val magicDecisiveStrikeDescription: MutableComponent = Component.translatable("matrix.decisive_strike.description")

    val magicManaOverload: MutableComponent = Component.translatable("matrix.mana_overload")
    val magicManaOverloadDescription: MutableComponent = Component.translatable("matrix.mana_overload.description")

    val magicHealthSteal: MutableComponent = Component.translatable("matrix.health_steal")
    val magicHealthStealDescription: MutableComponent = Component.translatable("matrix.health_steal.description")

    val magicExplosion: MutableComponent = Component.translatable("matrix.explosion")
    val magicExplosionDescription: MutableComponent = Component.translatable("matrix.explosion.description")

    val killMagic: MutableComponent = Component.translatable("matrix.kill")
    val killMagicDescription: MutableComponent = Component.translatable("matrix.kill.description")

    val sculkCatalystMagic: MutableComponent = Component.translatable("matrix.sculk_catalyst")
    val sculkCatalystMagicDescription: MutableComponent = Component.translatable("matrix.sculk_catalyst.description")

    val magicMemoryErase: MutableComponent = Component.translatable("matrix.memory_erase")
    val magicMemoryEraseDescription: MutableComponent = Component.translatable("matrix.memory_erase.description")

    val magicIgniteMagic: MutableComponent = Component.translatable("matrix.ignite")
    val magicIgniteMagicDescription: MutableComponent = Component.translatable("matrix.ignite.description")

    val magicBreakingBad: MutableComponent = Component.translatable("matrix.breaking_bad")
    val magicBreakingBadDescription: MutableComponent = Component.translatable("matrix.breaking_bad.description")

    val magicSpread: MutableComponent = Component.translatable("matrix.spread")
    val magicSpreadDescription: MutableComponent = Component.translatable("matrix.spread.description")

    val magicCrippleMovement: MutableComponent = Component.translatable("matrix.cripple_movement")
    val magicCrippleMovementDescription: MutableComponent = Component.translatable("matrix.cripple_movement.description")

    val magicSystemCrash: MutableComponent = Component.translatable("matrix.system_crash")
    val magicSystemCrashDescription: MutableComponent = Component.translatable("matrix.system_crash.description")

    val magicLightningBoltMagic: MutableComponent = Component.translatable("matrix.lightning_bolt")
    val magicLightningBoltMagicDescription: MutableComponent = Component.translatable("matrix.lightning_bolt.description")

    val magicArmorPenetrationMagic: MutableComponent = Component.translatable("matrix.armor_penetration")
    val magicArmorPenetrationMagicDescription: MutableComponent = Component.translatable("matrix.armor_penetration.description")

    val magicTeleport: MutableComponent = Component.translatable("matrix.teleport")
    val magicTeleportDescription: MutableComponent = Component.translatable("matrix.teleport.description")

    val magicSonicBoom: MutableComponent = Component.translatable("matrix.sonic_boom")
    val magicSonicBoomDescription: MutableComponent = Component.translatable("matrix.sonic_boom.description")

    val magicBruteForce: MutableComponent = Component.translatable("matrix.brute_force")
    val magicBruteForceDescription: MutableComponent = Component.translatable("matrix.brute_force.description")

    val magicBloodPact: MutableComponent = Component.translatable("matrix.blood_pact")
    val magicBloodPactDescription: MutableComponent = Component.translatable("matrix.blood_pact.description")

    val magicAttract: MutableComponent = Component.translatable("matrix.attract")
    val magicAttractDescription: MutableComponent = Component.translatable("matrix.attract.description")

    val magicLevitation: MutableComponent = Component.translatable("matrix.levitation")
    val magicLevitationDescription: MutableComponent = Component.translatable("matrix.levitation.description")

    val overclockMagic: MutableComponent = Component.translatable("matrix.overclock.magic")
    val overclockMana: MutableComponent = Component.translatable("matrix.overclock.mana")
    val switchClock: MutableComponent = Component.translatable("matrix.overclock.switch")

    val systemCrashing: MutableComponent = Component.translatable("matrix.system_is_crashing")

    val magicAvailable: MutableComponent = Component.translatable("matrix.magic_available")
    val magicAvailableManaNotEnough: MutableComponent = Component.translatable("matrix.magic_available.mana_not_enough")
    val magicTargetImmune: MutableComponent = Component.translatable("matrix.magic_target_immune")
    val magicUnavailable: MutableComponent = Component.translatable("matrix.magic_unavailable")
    val magicChannelQueueFull: MutableComponent = Component.translatable("matrix.magic_channel_queue_full")
    val magicChannelQueueLocked: MutableComponent = Component.translatable("matrix.magic_channel_queue_locked")
    val magicTargetMissing: MutableComponent = Component.translatable("matrix.magic_target_missing")
    val magicSculkCatalystIsAlreadyActive: MutableComponent = Component.translatable("matrix.sculk_catalyst_is_already_active")

    val wardenChestplateDescription: MutableComponent = Component.translatable("matrix.warden_chestplate.description")

    val redstoneSuitPower: MutableComponent = Component.translatable("matrix.redstone_suit.power")

    val redstoneHelmetDescription: MutableComponent = Component.translatable("matrix.redstone_helmet.power")
    val redstoneChestplateDescription: MutableComponent = Component.translatable("matrix.redstone_chestplate.description")
    val redstoneLeggingsDescription: MutableComponent = Component.translatable("matrix.redstone_leggings.description")
    val redstoneBootsDescription: MutableComponent = Component.translatable("matrix.redstone_boots.description")
    val redstoneSwordDescription: MutableComponent = Component.translatable("matrix.redstone_sword.description")
    val redstoneMiningToolDescription: MutableComponent = Component.translatable("matrix.redstone_mining_tool.description")

    val manaCostReduced: MutableComponent = Component.translatable("matrix.mana_cost_reduced")
    val manaCostIncreased: MutableComponent = Component.translatable("matrix.mana_cost_increased")

    val borrowedTimeChargeDescription: MutableComponent = Component.translatable("matrix.borrowed_time_charge.description")
    val wizardHelmetLoadDescription: MutableComponent = Component.translatable("matrix.wizard_helmet.load.description")

    val wizardHelmetBloodPactExchangeRate: MutableComponent = Component.translatable("matrix.wizard_helmet.blood_pact_exchange_rate")
    val wizardHelmetManaDeltaDescription: MutableComponent = Component.translatable("matrix.wizard_helmet.accumulated_mana_delta")
}