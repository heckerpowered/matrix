package heckerpowered.matrix.data.enchantment

import heckerpowered.matrix.common.enchantment.witherArmorEnchantmentKey
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
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
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
    }

    private fun register(
        entries: Entries,
        key: RegistryKey<Enchantment>,
        builder: Enchantment.Builder,
        vararg resourceConditions: ResourceCondition
    ) {
        entries.add(key, builder.build(key.value), *resourceConditions)
    }
}