package heckerpowered.matrix.common.item

import heckerpowered.matrix.Matrix
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

val allMatrixItems
    get() = MatrixItems.allItems

object MatrixItems {
    val allItems = arrayOf(
        WardenChestplateItem,
        RedstoneHelmetItem,
        RedstoneChestplateItem,
        RedstoneLeggingsItem,
        RedstoneBootsItem,
        RedstoneSwordItem,
        RedstonePickaxeItem,
        RedstoneAxeItem,
        RedstoneShovelItem,
        RedstoneHoeItem,
        LapisLazuliHelmetItem,
        LapisLazuliChestplateItem,
        LapisLazuliLeggingsItem,
        LapisLazuliBootsItem,
        LapisLazuliSwordItem,
        LapisLazuliPickaxeItem,
        LapisLazuliAxeItem,
        LapisLazuliShovelItem,
        LapisLazuliHoeItem,

        EmeraldHelmetItem,
        EmeraldChestplateItem,
        EmeraldLeggingsItem,
        EmeraldBootsItem,
        EmeraldSwordItem,
        EmeraldPickaxeItem,
        EmeraldAxeItem,
        EmeraldShovelItem,
        EmeraldHoeItem,

        CoalHelmetItem,
        CoalChestplateItem,
        CoalLeggingsItem,
        CoalBootsItem,
        CoalSwordItem,
        CoalPickaxeItem,
        CoalAxeItem,
        CoalShovelItem,
        CoalHoeItem,

        StoneHelmetItem,
        StoneChestplateItem,
        StoneLeggingsItem,
        StoneBootsItem,

        WoodenHelmetItem,
        WoodenChestplateItem,
        WoodenLeggingsItem,
        WoodenBootsItem,

        WizardHelmetHacker,
        WizardHelmetBasic,
        WizardHelmetDoom,
        WizardHelmetRuin,
        WizardHelmetApogee,
        WizardHelmetWarpDancer,

        LightningChestplateBorrowedTime
    )

    private fun register(item: Item, name: String): Item {
        val identifier = Matrix.identifier(name)
        val registeredItem = Registry.register(Registries.ITEM, identifier, item)
        return registeredItem
    }

    fun onInitialize() {
        register(WardenChestplateItem, "warden_chestplate")

        register(RedstoneHelmetItem, "redstone_helmet")
        register(RedstoneChestplateItem, "redstone_chestplate")
        register(RedstoneLeggingsItem, "redstone_leggings")
        register(RedstoneBootsItem, "redstone_boots")
        register(RedstoneSwordItem, "redstone_sword")
        register(RedstonePickaxeItem, "redstone_pickaxe")
        register(RedstoneAxeItem, "redstone_axe")
        register(RedstoneShovelItem, "redstone_shovel")
        register(RedstoneHoeItem, "redstone_hoe")

        register(LapisLazuliHelmetItem, "lapis_lazuli_helmet")
        register(LapisLazuliChestplateItem, "lapis_lazuli_chestplate")
        register(LapisLazuliLeggingsItem, "lapis_lazuli_leggings")
        register(LapisLazuliBootsItem, "lapis_lazuli_boots")
        register(LapisLazuliSwordItem, "lapis_lazuli_sword")
        register(LapisLazuliPickaxeItem, "lapis_lazuli_pickaxe")
        register(LapisLazuliAxeItem, "lapis_lazuli_axe")
        register(LapisLazuliShovelItem, "lapis_lazuli_shovel")
        register(LapisLazuliHoeItem, "lapis_lazuli_hoe")

        register(EmeraldHelmetItem, "emerald_helmet")
        register(EmeraldChestplateItem, "emerald_chestplate")
        register(EmeraldLeggingsItem, "emerald_leggings")
        register(EmeraldBootsItem, "emerald_boots")
        register(EmeraldSwordItem, "emerald_sword")
        register(EmeraldPickaxeItem, "emerald_pickaxe")
        register(EmeraldAxeItem, "emerald_axe")
        register(EmeraldShovelItem, "emerald_shovel")
        register(EmeraldHoeItem, "emerald_hoe")

        register(CoalHelmetItem, "coal_helmet")
        register(CoalChestplateItem, "coal_chestplate")
        register(CoalLeggingsItem, "coal_leggings")
        register(CoalBootsItem, "coal_boots")
        register(CoalSwordItem, "coal_sword")
        register(CoalPickaxeItem, "coal_pickaxe")
        register(CoalAxeItem, "coal_axe")
        register(CoalShovelItem, "coal_shovel")
        register(CoalHoeItem, "coal_hoe")

        register(StoneHelmetItem, "stone_helmet")
        register(StoneChestplateItem, "stone_chestplate")
        register(StoneLeggingsItem, "stone_leggings")
        register(StoneBootsItem, "stone_boots")

        register(WoodenHelmetItem, "wooden_helmet")
        register(WoodenChestplateItem, "wooden_chestplate")
        register(WoodenLeggingsItem, "wooden_leggings")
        register(WoodenBootsItem, "wooden_boots")

        register(WizardHelmetBasic, "wizard_helmet_basic")
        register(WizardHelmetHacker, "wizard_helmet_hacker")
        register(WizardHelmetDoom, "wizard_helmet_doom")
        register(WizardHelmetRuin, "wizard_helmet_ruin")
        register(WizardHelmetApogee, "wizard_helmet_apogee")
        register(WizardHelmetWarpDancer, "wizard_helmet_warp_dancer")

        register(LightningChestplateBorrowedTime, "lightning_chestplate_borrowed_time")
    }
}