package heckerpowered.matrix.data.enchantment

import heckerpowered.matrix.common.MagicManager
import heckerpowered.matrix.common.enchantment.*
import heckerpowered.matrix.common.item.MatrixItemTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition
import net.minecraft.component.type.AttributeModifierSlot
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.ItemTags
import java.util.concurrent.CompletableFuture

class EnchantmentGenerator(
    fabricDataOutput: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricDynamicRegistryProvider(fabricDataOutput, registriesFuture) {
    override fun getName(): String {
        return "MatrixEnchantmentGenerator"
    }

    override fun configure(registries: RegistryWrapper.WrapperLookup, entries: Entries) {
        register(
            entries, witherArmorEnchantmentKey, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.BODY
                )
            )
        )
        register(
            entries, guaranteedEnchantmentKey, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HAND
                )
            )
        )
        register(
            entries, lastStandEnchantmentKey, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HAND
                )
            )
        )
        register(
            entries, revivalEnchantmentKey, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.ARMOR
                )
            )
        )
        register(
            entries, secondWindEnchantmentKey, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.ARMOR
                )
            )
        )

        for (magic in MagicManager.getRegisteredMagics()) {
            register(
                entries, magic.enchantmentKey, Enchantment.builder(
                    Enchantment.definition(
                        registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                        10,
                        1,
                        Enchantment.leveledCost(1, 10),
                        Enchantment.leveledCost(1, 15),
                        5,
                        AttributeModifierSlot.HEAD
                    )
                )
            )
        }
        register(
            entries, proximatePropagationEnchantmentKey, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, magicQueue, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, queueAcceleration, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, queueMastery, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, manaOverflow, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, manaRegeneration, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, wizardForce, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, bloodPact, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    1,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, magicShield, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, brutalStrength, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
        register(
            entries, peakOverdrive, Enchantment.builder(
                Enchantment.definition(
                    registries.getWrapperOrThrow(RegistryKeys.ITEM).getOrThrow(MatrixItemTags.wizardHelmetTag),
                    10,
                    5,
                    Enchantment.leveledCost(1, 10),
                    Enchantment.leveledCost(1, 15),
                    5,
                    AttributeModifierSlot.HEAD
                )
            )
        )
    }

    private fun register(
        entries: Entries,
        key: RegistryKey<Enchantment>,
        builder: Enchantment.Builder,
        vararg resourceConditions: ResourceCondition,
    ) {
        entries.add(key, builder.build(key.value), *resourceConditions)
    }
}