/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.data.language

import heckerpowered.matrix.common.effect.*
import heckerpowered.matrix.common.enchantment.ModEnchantments
import heckerpowered.matrix.common.item.*
import heckerpowered.matrix.common.magic.spell.*
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

fun TranslationBuilder.addMagic(key: String, name: String) {
    add(key, name)
    add("enchantment.$key", name)
}

class ModEnglishLangProvider(
    fabricPackOutput: FabricPackOutput,
    registryLookup: CompletableFuture<HolderLookup.Provider>,
) : FabricLanguageProvider(fabricPackOutput, "en_us", registryLookup) {
    override fun generateTranslations(registryLookup: HolderLookup.Provider, translationBuilder: TranslationBuilder) {
        translationBuilder.add(MatrixLanguage.mana.key, "Mana")

        translationBuilder.add(TargetPositioningMagic.definition.name.key, "Target Positioning")
        translationBuilder.add(
            TargetPositioningMagic.definition.description.key,
            "Highlights the target and all entities within §a24§r meters for §a10§r seconds.\n\nUntraceable."
        )

        translationBuilder.add(DecisiveStrikeMagic.definition.name.key, "Decisive Strike")
        translationBuilder.add(
            DecisiveStrikeMagic.definition.description.key,
            "Deals §a6§r damage plus extra based on the player's base attack, and additional damage equal to §a14§r% of the target’s maximum health.\n\nIncreases damage by §a1§r% for each occupied slot in the channel queue (includes the active one), up to §a400§r%.\n\nWhen Blood Pact is active, damage is increased by §a100§r%.\n\nRemoves the target's brief invulnerability after being hit.\n\nTrackable."
        )

        translationBuilder.add(ManaOverloadMagic.definition.name.key, "Mana Overload")
        translationBuilder.add(
            ManaOverloadMagic.definition.description.key,
            "Overloads the target with mana, disabling their spell abilities.\n\nAffected enemies take §a15§r% more damage.\n\n§cSuppresses§r Endermen’s §dteleportation§r and §devasion§r.\n§cSuppresses§r Wardens’ §9sonic boom§r.\n§cSuppresses§r Illagers’ §call spells§r.\n§cSuppresses§r Guardians’ §9laser attacks§r and Elder Guardians’ mining fatigue.\nPrevents Witches from §cthrowing potions§r.\n\n§cSuppression§r immediately interrupts and prevents these abilities while the effect lasts.\n\nAt §a2§r stacks: removes and blocks beneficial effects.\n\nAt §a3§r stacks: deals §9continuous spell damage§r.\n\nAt max stacks: deals spell damage equal to §a100§r% of current health and prevents further stacking until the effect ends.\n\nTrackable."
        )

        translationBuilder.add(HealthStealMagic.definition.name.key, "Life Steal")
        translationBuilder.add(
            HealthStealMagic.definition.description.key,
            "Converts §a50§r% of the target's maximum health into your §6bonus health§r.\n\n§a50§r% of the converted value restores health, hunger, and saturation.\n\nBonus health gained this way cannot exceed your §cmaximum health§r.\n\nUntraceable."
        )

        translationBuilder.add(ExplosionMagic.definition.name.key, "Explosion")
        translationBuilder.add(
            ExplosionMagic.definition.description.key,
            "Creates an §cexplosion§r at the selected target's position.\n\nExplosion power: §a4§r.\n\nDamage is considered §9spell damage§r.\n\nDoes not break blocks and does not create §cfire§r.\n\nTrackable."
        )

        translationBuilder.add(KillMagic.definition.name.key, "Complete Erasure")
        translationBuilder.add(
            KillMagic.definition.description.key,
            "Instantly kills the target.\n\nTrackable."
        )

        translationBuilder.add(SculkCatalystMagic.definition.name.key, "Sculk Catalyst")
        translationBuilder.add(
            SculkCatalystMagic.definition.description.key,
            "Deals lethal damage to the target. On a kill, spreads to §a5§r enemies within §a25§r meters.\n\nEach spread automatically consumes mana; spread channel time becomes shorter while the mana cost increases. The cost can be halved if you are willing to pay in blood…\n\nTrackable.\n\n§7§o“Tell it to my code.”§r"
        )

        translationBuilder.add(MemoryWipeMagic.definition.name.key, "Memory Wipe")
        translationBuilder.add(
            MemoryWipeMagic.definition.description.key,
            "Forces the target to unlock and forget its current target.\n\nMagics queued after this become untraceable.\n\nDamage dealt by magics queued after this is not credited to you and will not trigger effects that only apply to you.\n\nUntraceable."
        )

        translationBuilder.add(IgniteMagic.definition.name.key, "Ignite")
        translationBuilder.add(
            IgniteMagic.definition.description.key,
            "§cIgnites§r the target for §a5§r seconds, dealing damage over time.\n\nIf the target is already §cignited§r, extending the effect sets its duration to §a8§r seconds.\n\n§cMelts§r armor: reduces the target's §a40§r% armor and toughness.\n\nIf the target is §2poisoned§r, §cdetonates§r it.\n\nExplosion power: §a4§r. Damage is §9spell damage§r. Does not break blocks and does not create §cfire§r.\n\nTrackable."
        )

        translationBuilder.add(BreakingBadMagic.definition.name.key, "Breaking Bad")
        translationBuilder.add(
            BreakingBadMagic.definition.description.key,
            "Applies §2poison§r and blindness to the target for §a5§r seconds.\n\nCan spread to §a4§r enemies within §a8§r meters.\n\nIf the target is §cignited§r, §cdetonates§r it.\n\nExplosion power: §a4§r. Damage is §9spell damage§r. Does not break blocks and does not create §cfire§r.\n\nTrackable."
        )

        translationBuilder.add(SpreadMagic.definition.name.key, "Contagion")
        translationBuilder.add(
            SpreadMagic.definition.description.key,
            "All magics queued after this spread to creatures within §a24§r meters.\n\nSpreading automatically consumes mana.\n\nThis magic itself cannot be spread.\n\nUntraceable."
        )

        translationBuilder.add(CrippleMovementMagic.definition.name.key, "Cripple Movement")
        translationBuilder.add(
            CrippleMovementMagic.definition.description.key,
            "Disables the target's §dteleportation§r and hinders movement for §a10§r seconds.\n\nCosts more mana and has a weaker effect against players.\n\nUntraceable."
        )

        translationBuilder.add(SystemCrashMagic.definition.name.key, "System Crash")
        translationBuilder.add(
            SystemCrashMagic.definition.description.key,
            "Effective only on players; crashes the target player's system.\n\nUntraceable."
        )

        translationBuilder.add(LightningBoltMagic.definition.name.key, "Lightning Whirlstrike")
        translationBuilder.add(
            LightningBoltMagic.definition.description.key,
            """
Summons lightning at the target; effects depend on color:

§4Red§r: deals §a400§r% damage.

§cOrange§r: removes all armor from hit entities for §a10§r seconds.

§eYellow§r: highlights the hit entity. Regardless of distance, immediately charges the held item and attacks the target once.

§aGreen§r: heals you for 1 heart on hit and applies §2poison§r to the target for §a10§r seconds.

§9Cyan§r: automatically channels §9Cripple Movement§r on the hit target at no mana cost. If already affected, applies the §cExposed§r state for §a10§r seconds instead.

§1Blue§r: channels this magic on the hit target.

§dPurple§r: doubles the area of effect, causes small explosions on each hit, and applies “Lightning Draw” to the target for §a180§r seconds.

White lightning: no special effect.

Clear lightning: instantly kills all hit entities.

The chance for clear lightning is §a0.6§r%; the remaining colors share the rest equally.
Player-summoned lightning behaves differently from natural lightning (does not charge Creepers, damage does not scale with difficulty, and does not trigger advancements).
When cast after “Memory Wipe,” effects related to you (such as healing you) do not apply.
Continuous use reduces mana cost by §a20§r% per stack, up to §a80§r%.

Untraceable.
""".trimIndent()
        )

        translationBuilder.add(ArmorPenetrationMagic.definition.name.key, "Armor Penetration")
        translationBuilder.add(
            ArmorPenetrationMagic.definition.description.key,
            "Reduces the target's §9armor§r and §9toughness§r by §a40§r% for §a10§r seconds.\n\nThis effect can be cleared with milk.\n\nUntraceable."
        )

        translationBuilder.add(TeleportMagic.definition.name.key, "Teleport")
        translationBuilder.add(
            TeleportMagic.definition.description.key,
            "Teleports to the target location and automatically attacks entities within §a3§r meters.\n\nTrackable."
        )

        translationBuilder.add(SonicBoomMagic.definition.name.key, "Sonic Shriek")
        translationBuilder.add(
            SonicBoomMagic.definition.description.key,
            "Channels a §9sonic boom§r, dealing §a10§r damage to the target.\n\nThe §9sonic boom§r ignores armor, any damage-reducing enchantments (e.g., Protection), and shield blocking; it does not trigger Thorns, and its damage to §9Witches§r is reduced by §a85§r%.\n\n§9Wither Armor§r can mitigate damage from the §9sonic boom§r.\n\nTrackable."
        )

        translationBuilder.add(BruteForceMagic.definition.name.key, "Brute Force")
        translationBuilder.add(
            BruteForceMagic.definition.description.key,
            "Applies the §cExposed§r state to the target for §a10§r seconds.\n\nTargets affected by §cExposed§r take §a100§r% additional damage; each extra level adds another §a100§r%.\n\nUntraceable."
        )

        translationBuilder.add(AttractMagic.definition.name.key, "The Hands")
        translationBuilder.add(
            AttractMagic.definition.description.key,
            "Creates an §cinvisible hand§r at the target location for §a6§r seconds.\n\nContinuously pulls nearby entities within §a6§r meters to its position.\n\nDoes not pull the caster.\n\nUntraceable."
        )

        translationBuilder.add(LevitationMagic.definition.name.key, "Levitation")
        translationBuilder.add(
            LevitationMagic.definition.description.key,
            "Applies §aLevitation§r to the target for §a10§r seconds.\n\nStacks; stacking increases the effect level and resets the duration, with no upper limit.\n\nUntraceable."
        )

        translationBuilder.add(AbsolvriftMagic.definition.name.key, "Absolvrift")
        translationBuilder.add(
            AbsolvriftMagic.definition.description.key,
            "Deal damage to the target and all enemies within §a6m§r, equal to §a100%§r of your Attack Damage.\n\nFor the next §a20§r seconds, automatically strike up to §a5§r enemies within §a8m§r, prioritized by distance, dealing §a100%§r of your Attack Damage once per second.\n\nEach strike reduces the target’s maximum health by §a2.5%§r for §a8§r seconds. This effect can stack, up to a maximum of §a50%§r.\n\nWhenever you deal damage, automatically perform an additional strike, dealing 75% of your Attack Damage, considered as an auto-attack.\n\nTrackable."
        )

        translationBuilder.add(TuckInMagic.definition.name.key, "Tuck In")
        translationBuilder.add(
            TuckInMagic.definition.description.key,
            "Deal Magic Damage to the target equal to §a500%§r of your Attack Damage.\n" +
                    "This damage is multiplied by the ratio of your §acurrent health + absorption§r to your §amax health§r.\n" +
                    "The multiplier has a minimum of §a30%§r and no upper limit.\n\n" +
                    "On cast, consume §a7.5%§r of your current (health + absorption), draining absorption first.\n\n" +
                    "Trackable."
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
        translationBuilder.add(MatrixLanguage.magicSculkCatalystIsAlreadyActive.key, "SCULK CATALYST IS ALREADY ACTIVE")

        translationBuilder.add(ArmorPenetrationEffect, "Armor Penetration")
        translationBuilder.add(ManaOverloadEffect, "Mana Overload")
        translationBuilder.add(CrippleMovementEffect, "Cripple Movement")
        translationBuilder.add(WitherArmorChargedEffect, "Wither Armor Charged")
        translationBuilder.add(WitherArmorEffect, "Wither Armor")
        translationBuilder.add(AngeredEffect, "Angered")
        translationBuilder.add(BloodPactEffect, "Blood Pact")
        translationBuilder.add(BorrowedTimeEffect, "Borrowed Time")

        translationBuilder.add(ModEnchantments.WitherArmor, "Wither Armor")
        translationBuilder.add(ModEnchantments.Guaranteed, "Guaranteed")
        translationBuilder.add(ModEnchantments.LastStand, "Last Stand")
        translationBuilder.add(ModEnchantments.Revival, "Revival")
        translationBuilder.add(ModEnchantments.SecondWind, "Second Wind")
        translationBuilder.add(ModEnchantments.ProximatePropagation, "Proximate Propagation")
        translationBuilder.add(ModEnchantments.ManaOverflow, "Mana Overflow")
        translationBuilder.add(ModEnchantments.ManaRegeneration, "Mana Regeneration")
        translationBuilder.add(ModEnchantments.WizardForce, "Wizard Force")
        translationBuilder.add(ModEnchantments.BloodPact, "Blood Pact")
        translationBuilder.add(ModEnchantments.MagicShield, "Magic Shield")
        translationBuilder.add(ModEnchantments.BrutalStrength, "Brutal Strength")
        translationBuilder.add(ModEnchantments.PeakOverdrive, "Peak Overdrive")
        translationBuilder.add(ModEnchantments.LightningStrike, "Lightning Strike")
        translationBuilder.add(ModEnchantments.KineticThrow, "Kinetic Throw")

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

        translationBuilder.add(ModCreativeTab.creativeTabKey, "Matrix")

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
        translationBuilder.add(WizardHelmet1, "Wizard 1 'Basic'")
        translationBuilder.add(WizardHelmet2, "Wizard 2 'Doom'")
        translationBuilder.add(WizardHelmet3, "Wizard 3 'Blood-forged Ruin'")
        translationBuilder.add(WizardHelmet4, "Wizard 4 'Might and Method'")
        translationBuilder.add(WizardHelmet5, "Wizard 5 'Axiom of Annihilation'")
        translationBuilder.add(WizardHelmet10, "Wizard X 'The Absent Presence'")
        translationBuilder.add(WizardHelmet13, "Wizard 13 'Overflux Crown'")

        translationBuilder.add(LightningChestplate1, "Lightning 1 'Warp Dancer'")

        translationBuilder.add(MagicTalismanItem, "Magic Talisman")
        translationBuilder.add(FinderArrowItem, "Finder")
        translationBuilder.add(MetaBowItem, "Meta Bow")

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
        translationBuilder.add(MatrixLanguage.wizardHelmetBloodPactExchangeRate.key, "Blood Pact Conversion Efficiency: ")
        translationBuilder.add(MatrixLanguage.wizardHelmetManaDeltaDescription.key, "Spent/Recovered Mana: ")
    }
}