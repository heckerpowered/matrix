package heckerpowered.matrix.data.language

import heckerpowered.matrix.common.effect.*
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.BLOOD_PACT_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.BRUTAL_STRENGTH_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.GUARANTEED_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.LAST_STAND_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MAGIC_SHIELD_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MANA_OVERFLOW_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MANA_REGENERATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PEAK_OVERDRIVE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PROXIMATE_PROPAGATION_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.REVIVAL_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.SECOND_WIND_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.WITHER_ARMOR_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.WIZARD_FORCE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.item.*
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

fun TranslationBuilder.addMagic(key: String, name: String) {
    add(key, name)
    add("enchantment.$key", name)
}

class MatrixModEnglishLangProvider(
    dataOutput: FabricDataOutput,
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricLanguageProvider(dataOutput, "en_us", registryLookup) {
    override fun generateTranslations(
        registryLookup: RegistryWrapper.WrapperLookup,
        translationBuilder: TranslationBuilder,
    ) {
        translationBuilder.add(MatrixLanguage.mana.key, "Mana")

        translationBuilder.addMagic(MatrixLanguage.magicTargetPositioning.key, "Target Positioning")
        translationBuilder.add(
            MatrixLanguage.magicTargetPositioningDescription.key,
            "Highlights the target and all entities within a §a24§r-meter radius for §a10§r seconds.\n\nUntraceable."
        )

        translationBuilder.addMagic(MatrixLanguage.magicDecisiveStrike.key, "Decisive Strike")
        translationBuilder.add(
            MatrixLanguage.magicDecisiveStrikeDescription.key,
            "Deals §a6§r damage to the target, adds extra damage based on the player's base attack damage, and inflicts additional damage equal to §a14§r% of the target's maximum health.\n\nIncreases damage by §a1§r% per magics' cost in the channel queue, up to §a400§r%.\n\nRemoves the target's invincibility frames upon being hit.\n\nTrackable."
        )

        translationBuilder.addMagic(MatrixLanguage.magicManaOverload.key, "Mana Overload")
        translationBuilder.add(
            MatrixLanguage.magicManaOverloadDescription.key,
            "Overloads the target with mana, disabling their spell abilities.\n\nDeal §a15§r% more damage to affected enemies.\n\n§cSuppresses§r Enderman's §dteleportation§r and §devasion§r abilities.\n§cSuppresses§r Wardens' ability to channel §9sonic boom§r.\n§cSuppresses§r Illagers' §call spells§r.\n§cSuppresses§r Guardians' §9laser attacks§r and Elder Guardians' ability to inflict §7mining fatigue§r on players.\nPrevents Witches from §cthrowing potions§r.\n\n§cSuppression§r immediately interrupts and prevents the use of related abilities for the effect's duration.\n\nRemoves and blocks beneficial effects on the target at §a2§r stacks.\n\nDeals §9continuous spell damage§r at §a3§r stacks.\n\nAt §cmaximum§r stacks, deals spell damage equal to 100% of target's current health and prevents further stacking until the effect ends.\n\nCan be tracked."
        )

        translationBuilder.addMagic(MatrixLanguage.magicHealthSteal.key, "Life Steal")
        translationBuilder.add(
            MatrixLanguage.magicHealthStealDescription.key,
            "Converts §a50§r% of the target's maximum health into additional health for the player.\n\n§a50§r% of the converted value is used to restore health, hunger, and saturation.\n\nUntraceable."
        )

        translationBuilder.addMagic(MatrixLanguage.magicExplosion.key, "Explosion")
        translationBuilder.add(
            MatrixLanguage.magicExplosionDescription.key,
            "Create an§c explosion§r at the selected target's location.\n\nTrackable."
        )

        translationBuilder.addMagic(MatrixLanguage.killMagic.key, "Complete Erasure")
        translationBuilder.add(
            MatrixLanguage.killMagicDescription.key, "Instantly kill the target.\n\nTrackable."
        )

        translationBuilder.addMagic(MatrixLanguage.sculkCatalystMagic.key, "Sculk Catalyst")
        translationBuilder.add(
            MatrixLanguage.sculkCatalystMagicDescription.key,
            "This magic spreads to 5 enemies with 20m., causing§c lethal damage§r to enemies. \n\nAutomatically consumes mana every time it spreads. Each spread has a shorter channel time and a higher mana cost than the last. But the cost can be cut in half if you're willing to pay in blood..."
        )

        translationBuilder.addMagic(MatrixLanguage.magicMemoryErase.key, "Memory Erasure")
        translationBuilder.add(
            MatrixLanguage.magicMemoryEraseDescription.key,
            "Forces the target to forget the currently locked target.\n\nMagics queued after this magic Untraceable. \n\nUntraceable."
        )

        translationBuilder.addMagic(MatrixLanguage.magicIgniteMagic.key, "Ignite")
        translationBuilder.add(
            MatrixLanguage.magicIgniteMagicDescription.key,
            "§cIgnites§r the target for §a10§r seconds, dealing continuous damage.\n\nIf the target is §2poisoned§r, it will§c explode§r.\n\nTrackable when causing an§c explosion§r."
        )

        translationBuilder.addMagic(MatrixLanguage.magicBreakingBad.key, "Breaking Bad")
        translationBuilder.add(
            MatrixLanguage.magicBreakingBadDescription.key,
            "Poisons and blinds the target for 10 seconds, dealing continuous damage.\n\nIf the target is ignited, it will explode.\n\nTrackable when causing an explosion."
        )

        translationBuilder.addMagic(MatrixLanguage.magicSpread.key, "Contagion")
        translationBuilder.add(
            MatrixLanguage.magicSpreadDescription.key,
            "All magics queued after this one will spread to creatures within §a24§r meters.\n\nCannot spread.\n\nUntraceable."
        )

        translationBuilder.addMagic(MatrixLanguage.magicCrippleMovement.key, "Cripple Movement")
        translationBuilder.add(
            MatrixLanguage.magicCrippleMovementDescription.key,
            "Disables the target's teleportation ability and hinders their movement for §a10§r seconds.\n\nUntraceable."
        )

        translationBuilder.addMagic(MatrixLanguage.magicSystemCrash.key, "System Crash")
        translationBuilder.add(
            MatrixLanguage.magicSystemCrashDescription.key,
            "Effective only on players, crashes the target player's system.\n\nUntraceable."
        )

        translationBuilder.addMagic(MatrixLanguage.magicLightningBoltMagic.key, "Lightning Whirlstrike")
        translationBuilder.add(
            MatrixLanguage.magicLightningBoltMagicDescription.key,
            """
    Summons lightning at the target's location, with different colors having different effects.
    
    §4Red§r lightning deals §a400§r% damage to hit entities.
    
    §cOrange§r lightning removes all armor from hit entities for §a10§r seconds.
    
    §eYellow§r lightning highlights the hit entity. Regardless of the distance, immediately charges the held item and attacks the target once.
    
    §aGreen§r lightning heals you for 1 heart upon hitting and poisons the target for §a10§r seconds.
    
    §9Cyan§r lightning automatically channels "Cripple Movement" on the hit target without extra mana cost. For targets already affected, it also applies the §cExposed§r state for §a10§r seconds.
    
    §1Blue§r lightning channels this magic on the hit target.
    
    §dPurple§r lightning increases the area of effect by §a100§r%, causes small explosions on each hit, and applies the "Lightning Strike" status to the target for §a180§r seconds.
    
    White lightning has no special effect.
    
    Clear lightning instantly kills all hit entities.
    
    The extraction probability for clear lightning is §a0.6§r%, and the probabilities for all other colors are equally distributed.
    Lightning summoned by the player behaves differently from natural lightning, such as not charging Creepers, damage not scaling with difficulty, and not triggering achievements.
    When cast after "Memory Erasure," this magic will render any effects related to you invalid, such as preventing healing.
    Continuous mana usage reduces mana consumption by §a20§r%, stackable up to §a80§r%.
    
    Untraceable.
    """.trimIndent()
        )

        translationBuilder.addMagic(MatrixLanguage.magicArmorPenetrationMagic.key, "Armor Penetration")
        translationBuilder.add(
            MatrixLanguage.magicArmorPenetrationMagicDescription.key,
            "Reduces the target's §9armor§r and §9thoughness§r by §a40§r% for §a10§r seconds.\n\nUntraceable."
        )

        translationBuilder.addMagic(MatrixLanguage.magicTeleport.key, "Teleport")
        translationBuilder.add(
            MatrixLanguage.magicTeleportDescription.key,
            "Teleports to the target's location and attacks entities surrounds the target within 3m.\n\nUntraceable."
        )

        translationBuilder.addMagic(MatrixLanguage.magicSonicBoom.key, "Sonic Shriek")
        translationBuilder.add(
            MatrixLanguage.magicSonicBoomDescription.key,
            "Channel a §9sonic boom§r, dealing §a10§r damage to the target.\n\nThe §9sonic boom§r ignores armor, any damage-reducing enchantments (e.g., Protection), and shield blocking. It does not trigger the Thorns enchantment, and its damage to §9witches§r is reduced by §a85§r%.\n\n§9Wither Armor§r can mitigate the damage caused by the §9sonic boom§r.\n\nTrackable."
        )

        translationBuilder.addMagic(MatrixLanguage.magicBruteForce.key, "Brute Force")
        translationBuilder.add(MatrixLanguage.magicBruteForceDescription.key, "§cExpose§r the target for §a10§r seconds.\n\nEntities affected by the §cExposed§r effect take §a100§r% additional damage, with each level further increasing the damage by §a100§r%.\n\nUntraceable.")

        // translationBuilder.addMagic(MatrixLanguage.magicBloodPact.key, "Blood Pact")
        // translationBuilder.add(MatrixLanguage.magicBloodPactDescription.key, "Curse the target, transferring §a50§r% of the damage you take to the target, with no limit on the amount transferred.\n\nIf the target cannot withstand the transferred damage, the amount transferred will be reduced.\n\nIf multiple targets are cursed, they will all share the transferred damage.\n\nThe effect lasts as long as the curse remains.")

        translationBuilder.addMagic(MatrixLanguage.magicAttract.key, "The Hands")
        translationBuilder.add(
            MatrixLanguage.magicAttractDescription.key,
            "§cThe invisible hand exerts its force§r, pulling entities within §a6§r meters of the target to the target's location. §r\n\nUntraceable."
        )

        translationBuilder.addMagic(MatrixLanguage.magicLevitation.key, "Levitation")
        translationBuilder.add(MatrixLanguage.magicLevitationDescription.key, "Levitate the target for 10 seconds.\n\nCan be stacked, with stacking increasing the effect level and resetting the duration, with no limit.\n\nUntraceable. ")

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
        translationBuilder.add(MatrixLanguage.magicSculkCatalystIsAlreadyActive.key, "SCULK CATALYST IS ALREADY ACTIVE")

        translationBuilder.add(ArmorPenetrationEffect, "Armor Penetration")
        translationBuilder.add(ManaOverloadEffect, "Mana Overload")
        translationBuilder.add(CrippleMovementEffect, "Cripple Movement")
        translationBuilder.add(WitherArmorChargedEffect, "Wither Armor Charged")
        translationBuilder.add(WitherArmorEffect, "Wither Armor")
        translationBuilder.add(AngeredEffect, "Angered")
        translationBuilder.add(BloodPactEffect, "Blood Pact")
        translationBuilder.add(BorrowedTimeEffect, "Borrowed Time")

        translationBuilder.add(WITHER_ARMOR_ENCHANTMENT_KEY, "Wither Armor")
        translationBuilder.add(GUARANTEED_ENCHANTMENT_KEY, "Guaranteed")
        translationBuilder.add(LAST_STAND_ENCHANTMENT_KEY, "Last Stand")
        translationBuilder.add(REVIVAL_ENCHANTMENT_KEY, "Revival")
        translationBuilder.add(SECOND_WIND_ENCHANTMENT_KEY, "Second Wind")
        translationBuilder.add(PROXIMATE_PROPAGATION_ENCHANTMENT_KEY, "Proximate Propagation")
        translationBuilder.add(MANA_OVERFLOW_ENCHANTMENT_KEY, "Mana Overflow")
        translationBuilder.add(MANA_REGENERATION_ENCHANTMENT_KEY, "Mana Regeneration")
        translationBuilder.add(WIZARD_FORCE_ENCHANTMENT_KEY, "Wizard Force")
        translationBuilder.add(BLOOD_PACT_ENCHANTMENT_KEY, "Blood Pact")
        translationBuilder.add(MAGIC_SHIELD_ENCHANTMENT_KEY, "Magic Shield")
        translationBuilder.add(BRUTAL_STRENGTH_ENCHANTMENT_KEY, "Brutal Strength")
        translationBuilder.add(PEAK_OVERDRIVE_ENCHANTMENT_KEY, "Peak Overdrive")

        translationBuilder.add(WardenChestplateItem, "Sculk 'Warden' Chestplate")
        translationBuilder.add(
            MatrixLanguage.wardenChestplateDescription.key,
            """
                §7When Angered:§r
                Immune to §9damage§r, §9knockback§r, §9fire§r, and §9movement penalties§r.
                Clears and blocks all negative effects.
                Damage is increased by §9100%§r.
                -§9100%§r melee weapon charge time.
                Movement speed is increased.
            """.trimIndent()
        )

        translationBuilder.add(itemGroupKey, "Matrix")

        translationBuilder.add(MatrixLanguage.redstoneSuitPower.key, "Power: ")

        translationBuilder.add(RedstoneHelmetItem, "Redstone Helmet")
        translationBuilder.add(
            MatrixLanguage.redstoneHelmetDescription.key,
            "Electrolytic Breathing: Enables underwater breathing, using §91§r power every §95§r seconds."
        )

        translationBuilder.add(RedstoneChestplateItem, "Redstone Chestplate")
        translationBuilder.add(
            MatrixLanguage.redstoneChestplateDescription.key,
            "Consumes power to reduce damage taken: 1 power reduces 4 damage, up to 40% per hit."
        )
        translationBuilder.add(RedstoneLeggingsItem, "Redstone Leggings")
        translationBuilder.add(
            MatrixLanguage.redstoneLeggingsDescription.key,
            "Chance to damage nearby entities when hit."
        )
        translationBuilder.add(RedstoneBootsItem, "Redstone Boots")
        translationBuilder.add(RedstoneSwordItem, "Redstone Sword")
        translationBuilder.add(RedstonePickaxeItem, "Redstone Pickaxe")
        translationBuilder.add(RedstoneAxeItem, "Redstone Axe")
        translationBuilder.add(RedstoneShovelItem, "Redstone Shovel")
        translationBuilder.add(RedstoneHoeItem, "Redstone Hoe")
        translationBuilder.add(
            MatrixLanguage.redstoneMiningToolDescription.key,
            "Increases mining speed by 40%. Breaking blocks consumes 1 power."
        )
        translationBuilder.add(
            MatrixLanguage.redstoneSwordDescription.key,
            "Consumes 1 MEU power on attack to deal an additional 2 damage."
        )

        translationBuilder.add(LapisLazuliHelmetItem, "Lapis Lazuli Helmet")
        translationBuilder.add(LapisLazuliChestplateItem, "Lapis Lazuli Chestplate")
        translationBuilder.add(LapisLazuliLeggingsItem, "Lapis Lazuli Leggings")
        translationBuilder.add(LapisLazuliBootsItem, "Lapis Lazuli Boots")
        translationBuilder.add(LapisLazuliSwordItem, "Lapis Lazuli Sword")
        translationBuilder.add(LapisLazuliPickaxeItem, "Lapis Lazuli Pickaxe")
        translationBuilder.add(LapisLazuliAxeItem, "Lapis Lazuli Axe")
        translationBuilder.add(LapisLazuliShovelItem, "Lapis Lazuli Shovel")
        translationBuilder.add(LapisLazuliHoeItem, "Lapis Lazuli Hoe")

        translationBuilder.add(EmeraldHelmetItem, "Emerald Helmet")
        translationBuilder.add(EmeraldChestplateItem, "Emerald Chestplate")
        translationBuilder.add(EmeraldLeggingsItem, "Emerald Leggings")
        translationBuilder.add(EmeraldBootsItem, "Emerald Boots")
        translationBuilder.add(EmeraldSwordItem, "Emerald Sword")
        translationBuilder.add(EmeraldPickaxeItem, "Emerald Pickaxe")
        translationBuilder.add(EmeraldAxeItem, "Emerald Axe")
        translationBuilder.add(EmeraldShovelItem, "Emerald Shovel")
        translationBuilder.add(EmeraldHoeItem, "Emerald Hoe")

        translationBuilder.add(CoalHelmetItem, "Coal Helmet")
        translationBuilder.add(CoalChestplateItem, "Coal Chestplate")
        translationBuilder.add(CoalLeggingsItem, "Coal Leggings")
        translationBuilder.add(CoalBootsItem, "Coal Boots")
        translationBuilder.add(CoalSwordItem, "Coal Sword")
        translationBuilder.add(CoalPickaxeItem, "Coal Pickaxe")
        translationBuilder.add(CoalAxeItem, "Coal Axe")
        translationBuilder.add(CoalShovelItem, "Coal Shovel")
        translationBuilder.add(CoalHoeItem, "Coal Hoe")

        translationBuilder.add(StoneHelmetItem, "Stone Helmet")
        translationBuilder.add(StoneChestplateItem, "Stone Chestplate")
        translationBuilder.add(StoneLeggingsItem, "Stone Leggings")
        translationBuilder.add(StoneBootsItem, "Stone Boots")

        translationBuilder.add(WoodenHelmetItem, "Wooden Helmet")
        translationBuilder.add(WoodenChestplateItem, "Wooden Chestplate")
        translationBuilder.add(WoodenLeggingsItem, "Wooden Leggings")
        translationBuilder.add(WoodenBootsItem, "Wooden Boots")

        translationBuilder.add(WizardHelmetHacker, "Wizard 9000 'Hacker'")
        translationBuilder.add(WizardHelmetBasic, "Wizard 1 'Basic Wizard Helmet'")
        translationBuilder.add(WizardHelmetDoom, "Wizard 2 'Doom'")
        translationBuilder.add(WizardHelmetRuin, "Wizard 3 'Ruin'")
        translationBuilder.add(WizardHelmetApogee, "Wizard 4 'Apogee'")
        translationBuilder.add(WizardHelmetWarpDancer, "Wizard 5 'Warp Dancer'")

        translationBuilder.add(LightningChestplateBorrowedTime, "Lightning 1 'Borrowed Time'")

        translationBuilder.add(MagicTalismanItem, "Magic Talisman")

        // Potions
        translationBuilder.add("item.minecraft.potion.effect.angered", "Potion of Angered")
        translationBuilder.add("item.minecraft.splash_potion.effect.angered", "Slash Potion of Angered")
        translationBuilder.add("item.minecraft.lingering_potion.effect.angered", "Lingering Potion of Angered")

        translationBuilder.add("key.categories.matrix", "Matrix")
        translationBuilder.add("key.matrix.use_magic", "Channel magic")
        translationBuilder.add("key.matrix.next_magic", "Next magic")
        translationBuilder.add("key.matrix.previous_magic", "Previous magic")

        translationBuilder.add(MatrixLanguage.manaCostReduced.key, "Mana cost reduced by")
        translationBuilder.add(MatrixLanguage.manaCostIncreased.key, "Mana cost increased by")

        translationBuilder.add(MatrixLanguage.borrowedTimeChargeDescription.key, "Borrowed Time Charge: ")
        translationBuilder.add(MatrixLanguage.wizardHelmetLoadDescription.key, "Current load: ")
    }
}