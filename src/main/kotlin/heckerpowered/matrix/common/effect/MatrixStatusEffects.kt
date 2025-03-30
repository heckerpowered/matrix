package heckerpowered.matrix.common.effect

import heckerpowered.matrix.Matrix
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry

val manaOverloadEffect: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(ManaOverloadEffect) }
val armorPenetrationEffect: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(ArmorPenetrationEffect) }
val crippleMovementEffect: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(CrippleMovementEffect) }
val witherArmorChargedEffect: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(WitherArmorChargedEffect) }
val witherArmorEffect: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(WitherArmorEffect) }
val angeredEffect: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(AngeredEffect) }
val exposedEffect: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(ExposedEffect) }
val sculkCatalystEffect: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(SculkCatalystEffect) }
val bloodPactEffect: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(BloodPactEffect) }
val borrowedTimeEffect: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(BorrowedTimeEffect) }

object MatrixStatusEffects {
    fun onInitialize() {
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("mana_overload"), ManaOverloadEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("armor_penetration"), ArmorPenetrationEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("cripple_movement"), CrippleMovementEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("wither_armor_charged"), WitherArmorChargedEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("wither_armor"), WitherArmorEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("angered"), AngeredEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("exposed"), ExposedEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("sculk_catalyst"), SculkCatalystEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("blood_pact"), BloodPactEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("lightning"), BorrowedTimeEffect)
    }
}