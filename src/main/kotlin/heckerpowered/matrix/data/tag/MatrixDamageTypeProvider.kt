package heckerpowered.matrix.data.tag

import heckerpowered.matrix.common.tag.MatrixDamageTypeTags
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.entity.damage.DamageType
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.DamageTypeTags
import java.util.concurrent.CompletableFuture

class MatrixDamageTypeProvider(
    fabricDataOutput: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricTagProvider<DamageType>(fabricDataOutput, RegistryKeys.DAMAGE_TYPE, registriesFuture) {
    override fun configure(registries: RegistryWrapper.WrapperLookup) {
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_SHIELD)
            .add(MatrixDamageTypes.magic)

        getOrCreateTagBuilder(MatrixDamageTypeTags.magic)
            .add(MatrixDamageTypes.magic)
    }
}