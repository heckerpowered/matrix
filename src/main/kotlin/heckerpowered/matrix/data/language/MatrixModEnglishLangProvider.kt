package heckerpowered.matrix.data.language

import heckerpowered.matrix.common.effect.*
import heckerpowered.matrix.common.enchantment.witherArmorEnchantmentKey
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class MatrixModEnglishLangProvider(
    dataOutput: FabricDataOutput,
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricLanguageProvider(dataOutput, "en_us", registryLookup) {
    override fun generateTranslations(
        registryLookup: RegistryWrapper.WrapperLookup,
        translationBuilder: TranslationBuilder
    ) {
        translationBuilder.add(MatrixLanguage.mana.key, "Mana")

        translationBuilder.add(MatrixLanguage.magicTargetPositioning.key, "Target Positioning")
        translationBuilder.add(
            MatrixLanguage.magicTargetPositioningDescription.key,
            "Highlights the target and all entities within a 24-meter radius for 10 seconds.\n\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicDecisiveStrike.key, "Decisive Strike")
        translationBuilder.add(
            MatrixLanguage.magicDecisiveStrikeDescription.key,
            "Deals 6 damage to the target, adds extra damage based on the player's base attack damage, and inflicts additional damage equal to 14% of the target's maximum health.\n\nIncreases damage by 1% per magics' cost in the channel queue, up to 400%.\n\nRemoves the target's invincibility frames upon being hit.\n\nTrackable."
        )

        translationBuilder.add(MatrixLanguage.magicManaOverload.key, "Mana Overload")
        translationBuilder.add(
            MatrixLanguage.magicManaOverloadDescription.key,
            "Marks a target for 10 seconds. Killing the target during the mark duration restores 20 mana points.\n\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicHealthSteal.key, "Life Steal")
        translationBuilder.add(
            MatrixLanguage.magicHealthStealDescription.key,
            "Converts 50% of the target's maximum health into additional health for the player.\n\n50% of the converted value is used to restore health, hunger, and saturation.\n\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicExplosion.key, "Explosion")
        translationBuilder.add(
            MatrixLanguage.magicExplosionDescription.key,
            "Create an explosion at the selected target's location.\n\nTrackable."
        )

        translationBuilder.add(MatrixLanguage.killMagic.key, "Complete Erasure")
        translationBuilder.add(
            MatrixLanguage.killMagicDescription.key, "Instantly kill the target.\n\nTrackable."
        )

        translationBuilder.add(MatrixLanguage.sculkCatalystMagic.key, "Sculk Catalyst")
        translationBuilder.add(
            MatrixLanguage.sculkCatalystMagicDescription.key,
            "After a brief delay, deals massive damage to the target. If the target dies, the magic spreads to the next entity within 20 meters, consuming mana automatically. Each spread reduces the required time, increases the mana cost, and deals more damage. Automatically highlights the spread target."
        )

        translationBuilder.add(MatrixLanguage.magicMemoryErase.key, "Memory Erasure")
        translationBuilder.add(
            MatrixLanguage.magicMemoryEraseDescription.key,
            "Forces the target to forget the currently locked target.\n\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicIgniteMagic.key, "Ignite")
        translationBuilder.add(
            MatrixLanguage.magicIgniteMagicDescription.key,
            "Ignites the target for 10 seconds, dealing continuous damage.\n\nIf the target is poisoned, it will explode.\n\nTrackable when causing an explosion."
        )

        translationBuilder.add(MatrixLanguage.magicBreakingBad.key, "Breaking Bad")
        translationBuilder.add(
            MatrixLanguage.magicBreakingBadDescription.key,
            "Poisons and blinds the target for 10 seconds, dealing continuous damage.\n\nIf the target is ignited, it will explode.\n\nTrackable when causing an explosion."
        )

        translationBuilder.add(MatrixLanguage.magicSpread.key, "Contagion")
        translationBuilder.add(
            MatrixLanguage.magicSpreadDescription.key,
            "All magics queued after this one will spread to creatures within 24 meters.\n\nCannot spread.\n\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicCrippleMovement.key, "Cripple Movement")
        translationBuilder.add(
            MatrixLanguage.magicCrippleMovementDescription.key,
            "Disables the target's teleportation ability and hinders their movement for 10 seconds.\n\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicSystemCrash.key, "System Crash")
        translationBuilder.add(
            MatrixLanguage.magicSystemCrashDescription.key,
            "Effective only on players, crashes the target player's system.\n\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicLightningBoltMagic.key, "Lightning Whirlstrike")
        translationBuilder.add(
            MatrixLanguage.magicLightningBoltMagicDescription.key,
            "Summons lightning at the target's location.\n\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicArmorPenetrationMagic.key, "Armor Penetration")
        translationBuilder.add(
            MatrixLanguage.magicArmorPenetrationMagicDescription.key,
            "Reduces the target's armor and thoughness by 40% for 10 seconds.\n\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicTeleport.key, "Teleport")
        translationBuilder.add(
            MatrixLanguage.magicTeleportDescription.key,
            "Grants invisibility for 10 seconds and teleports to the target's location.\n\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.overclockMagic.key, "Overclock or Underclock Magic")
        translationBuilder.add(MatrixLanguage.overclockMana.key, "Overclock or Underclock Mana")
        translationBuilder.add(MatrixLanguage.switchClock.key, "Switch Overclock or Underclock")
        translationBuilder.add(MatrixLanguage.systemCrashing.key, "System Crash Imminent")

        translationBuilder.add(MatrixLanguage.magicAvailable.key, "Ready")
        translationBuilder.add(MatrixLanguage.magicAvailableManaNotEnough.key, "Insufficient Mana Available")
        translationBuilder.add(MatrixLanguage.magicUnavailable.key, "Unable to Channel Magic")
        translationBuilder.add(MatrixLanguage.magicChannelQueueFull.key, "Queue Full")
        translationBuilder.add(MatrixLanguage.magicChannelQueueLocked.key, "Queue Locked")
        translationBuilder.add(MatrixLanguage.magicTargetMissing.key, "Target Missing")
        translationBuilder.add(MatrixLanguage.magicTargetImmune.key, "Target Immune")

        translationBuilder.add(ArmorPenetrationEffect, "Armor Penetration")
        translationBuilder.add(ManaOverloadEffect, "Mana Overload")
        translationBuilder.add(CrippleMovementEffect, "Cripple Movement")
        translationBuilder.add(WitherArmorChargedEffect, "Wither Armor Charged")
        translationBuilder.add(WitherArmorEffect, "Wither Armor")

        translationBuilder.add(witherArmorEnchantmentKey, "Wither Armor")
    }
}