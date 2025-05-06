package heckerpowered.matrix.data.language

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.RegistryKey
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.Util

val MutableText.key: String
    get() {
        return (content as TranslatableTextContent).key
    }

fun TranslationBuilder.add(enchantment: RegistryKey<Enchantment>, value: String) {
    addEnchantment(enchantment, value)
}

fun TranslationBuilder.addEnchantmentDescription(enchantment: RegistryKey<Enchantment>, value: String) {
    add("${Util.createTranslationKey("enchantment", enchantment.value)}.desc", value)
}

object MatrixLanguage {
    val mana: MutableText = Text.translatable("matrix.mana")

    val magicTargetPositioning: MutableText = Text.translatable("matrix.target_positioning")
    val magicTargetPositioningDescription: MutableText = Text.translatable("matrix.target_positioning.description")

    val magicDecisiveStrike: MutableText = Text.translatable("matrix.decisive_strike")
    val magicDecisiveStrikeDescription: MutableText = Text.translatable("matrix.decisive_strike.description")

    val magicManaOverload: MutableText = Text.translatable("matrix.mana_overload")
    val magicManaOverloadDescription: MutableText = Text.translatable("matrix.mana_overload.description")

    val magicHealthSteal: MutableText = Text.translatable("matrix.health_steal")
    val magicHealthStealDescription: MutableText = Text.translatable("matrix.health_steal.description")

    val magicExplosion: MutableText = Text.translatable("matrix.explosion")
    val magicExplosionDescription: MutableText = Text.translatable("matrix.explosion.description")

    val killMagic: MutableText = Text.translatable("matrix.kill")
    val killMagicDescription: MutableText = Text.translatable("matrix.kill.description")

    val sculkCatalystMagic: MutableText = Text.translatable("matrix.sculk_catalyst")
    val sculkCatalystMagicDescription: MutableText = Text.translatable("matrix.sculk_catalyst.description")

    val magicMemoryErase: MutableText = Text.translatable("matrix.memory_erase")
    val magicMemoryEraseDescription: MutableText = Text.translatable("matrix.memory_erase.description")

    val magicIgniteMagic: MutableText = Text.translatable("matrix.ignite")
    val magicIgniteMagicDescription: MutableText = Text.translatable("matrix.ignite.description")

    val magicBreakingBad: MutableText = Text.translatable("matrix.breaking_bad")
    val magicBreakingBadDescription: MutableText = Text.translatable("matrix.breaking_bad.description")

    val magicSpread: MutableText = Text.translatable("matrix.spread")
    val magicSpreadDescription: MutableText = Text.translatable("matrix.spread.description")

    val magicCrippleMovement: MutableText = Text.translatable("matrix.cripple_movement")
    val magicCrippleMovementDescription: MutableText = Text.translatable("matrix.cripple_movement.description")

    val magicSystemCrash: MutableText = Text.translatable("matrix.system_crash")
    val magicSystemCrashDescription: MutableText = Text.translatable("matrix.system_crash.description")

    val magicLightningBoltMagic: MutableText = Text.translatable("matrix.lightning_bolt")
    val magicLightningBoltMagicDescription: MutableText = Text.translatable("matrix.lightning_bolt.description")

    val magicArmorPenetrationMagic: MutableText = Text.translatable("matrix.armor_penetration")
    val magicArmorPenetrationMagicDescription: MutableText = Text.translatable("matrix.armor_penetration.description")

    val magicTeleport: MutableText = Text.translatable("matrix.teleport")
    val magicTeleportDescription: MutableText = Text.translatable("matrix.teleport.description")

    val magicSonicBoom: MutableText = Text.translatable("matrix.sonic_boom")
    val magicSonicBoomDescription: MutableText = Text.translatable("matrix.sonic_boom.description")

    val magicBruteForce: MutableText = Text.translatable("matrix.brute_force")
    val magicBruteForceDescription: MutableText = Text.translatable("matrix.brute_force.description")

    val magicBloodPact: MutableText = Text.translatable("matrix.blood_pact")
    val magicBloodPactDescription: MutableText = Text.translatable("matrix.blood_pact.description")

    val magicAttract: MutableText = Text.translatable("matrix.attract")
    val magicAttractDescription: MutableText = Text.translatable("matrix.attract.description")

    val magicLevitation: MutableText = Text.translatable("matrix.levitation")
    val magicLevitationDescription: MutableText = Text.translatable("matrix.levitation.description")

    val overclockMagic: MutableText = Text.translatable("matrix.overclock.magic")
    val overclockMana: MutableText = Text.translatable("matrix.overclock.mana")
    val switchClock: MutableText = Text.translatable("matrix.overclock.switch")

    val systemCrashing: MutableText = Text.translatable("matrix.system_is_crashing")

    val magicAvailable: MutableText = Text.translatable("matrix.magic_available")
    val magicAvailableManaNotEnough: MutableText = Text.translatable("matrix.magic_available.mana_not_enough")
    val magicTargetImmune: MutableText = Text.translatable("matrix.magic_target_immune")
    val magicUnavailable: MutableText = Text.translatable("matrix.magic_unavailable")
    val magicChannelQueueFull: MutableText = Text.translatable("matrix.magic_channel_queue_full")
    val magicChannelQueueLocked: MutableText = Text.translatable("matrix.magic_channel_queue_locked")
    val magicTargetMissing: MutableText = Text.translatable("matrix.magic_target_missing")
    val magicSculkCatalystIsAlreadyActive: MutableText = Text.translatable("matrix.sculk_catalyst_is_already_active")

    val wardenChestplateDescription: MutableText = Text.translatable("matrix.warden_chestplate.description")

    val redstoneSuitPower: MutableText = Text.translatable("matrix.redstone_suit.power")

    val redstoneHelmetDescription: MutableText = Text.translatable("matrix.redstone_helmet.power")
    val redstoneChestplateDescription: MutableText = Text.translatable("matrix.redstone_chestplate.description")
    val redstoneLeggingsDescription: MutableText = Text.translatable("matrix.redstone_leggings.description")
    val redstoneBootsDescription: MutableText = Text.translatable("matrix.redstone_boots.description")
    val redstoneSwordDescription: MutableText = Text.translatable("matrix.redstone_sword.description")
    val redstoneMiningToolDescription: MutableText = Text.translatable("matrix.redstone_mining_tool.description")

    val manaCostReduced: MutableText = Text.translatable("matrix.mana_cost_reduced")
    val manaCostIncreased: MutableText = Text.translatable("matrix.mana_cost_increased")

    val borrowedTimeChargeDescription: MutableText = Text.translatable("matrix.borrowed_time_charge.description")
    val wizardHelmetLoadDescription: MutableText = Text.translatable("matrix.wizard_helmet.load.description")
}