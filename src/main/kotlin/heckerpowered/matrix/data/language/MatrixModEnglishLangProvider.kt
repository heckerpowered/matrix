package heckerpowered.matrix.data.language

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
            "Highlight the target and nearby entities.\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicDecisiveStrike.key, "Decisive Strike")
        translationBuilder.add(
            MatrixLanguage.magicDecisiveStrikeDescription.key,
            "Deals 6 damage to the target.\nRemoves the target’s invincibility frames upon being hit.\nTrackable."
        )

        translationBuilder.add(MatrixLanguage.magicManaOverload.key, "Mana Overload")
        translationBuilder.add(
            MatrixLanguage.magicManaOverloadDescription.key,
            "Marks a target for 5 seconds. Killing the target during the mark duration restores 20 mana points.\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicHealthSteal.key, "Life Steal")
        translationBuilder.add(
            MatrixLanguage.magicHealthStealDescription.key,
            "Convert the target's health into additional health for yourself.\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicExplosion.key, "Explosion")
        translationBuilder.add(
            MatrixLanguage.magicExplosionDescription.key,
            "Create an explosion at the selected target's location.\nTrackable."
        )

        translationBuilder.add(MatrixLanguage.killMagic.key, "Complete Erasure")
        translationBuilder.add(
            MatrixLanguage.killMagicDescription.key, "Instantly kill the target.\nTrackable."
        )

        translationBuilder.add(MatrixLanguage.sculkCatalystMagic.key, "Sculk Catalyst")
        translationBuilder.add(
            MatrixLanguage.sculkCatalystMagicDescription.key,
            "After a brief delay, deals massive damage to the target. If the target dies, the magic spreads to the next entity within 20 meters, consuming mana automatically. Each spread reduces the required time, increases the mana cost, and deals more damage. Automatically highlights the spread target."
        )

        translationBuilder.add(MatrixLanguage.magicMemoryErase.key, "Memory Erasure")
        translationBuilder.add(
            MatrixLanguage.magicMemoryEraseDescription.key,
            "Forces the target to forget the currently locked target.\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicIgniteMagic.key, "Ignite")
        translationBuilder.add(
            MatrixLanguage.magicIgniteMagicDescription.key,
            "Ignites the target for 5 seconds, dealing continuous damage.\nIf the target is poisoned, it will explode.\nTrackable when causing an explosion."
        )

        translationBuilder.add(MatrixLanguage.magicBreakingBad.key, "Breaking Bad")
        translationBuilder.add(
            MatrixLanguage.magicBreakingBadDescription.key,
            "Poisons the target for 5 seconds, dealing continuous damage.\nIf the enemy is ignited, it will explode.\nTrackable when causing an explosion."
        )

        translationBuilder.add(MatrixLanguage.magicSpread.key, "Contagion")
        translationBuilder.add(
            MatrixLanguage.magicSpreadDescription.key,
            "All skills queued after this one will spread to creatures within 24 meters.\nCannot spread.\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicCrippleMovement.key, "Cripple Movement")
        translationBuilder.add(
            MatrixLanguage.magicCrippleMovementDescription.key,
            "Slows the target for 5 seconds, reducing its movement speed.\nCannot be tracked."
        )

        translationBuilder.add(MatrixLanguage.magicSystemCrash.key, "System Crash")
        translationBuilder.add(
            MatrixLanguage.magicSystemCrashDescription.key,
            "Effective only on players, crashes the target player's system.\nCannot be tracked."
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
    }
}