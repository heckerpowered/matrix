package heckerpowered.matrix.common.effect

import heckerpowered.matrix.Matrix
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry

object MatrixStatusEffects {
    val MANA_OVERLOAD_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(ManaOverloadEffect) }
    val ARMOR_PENETRATION_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(ArmorPenetrationEffect) }
    val CRIPPLE_MOVEMENT_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(CrippleMovementEffect) }
    val WITHER_ARMOR_CHARGED_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(WitherArmorChargedEffect) }
    val WITHER_ARMOR_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(WitherArmorEffect) }
    val ANGERED_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(AngeredEffect) }
    val EXPOSED_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(ExposedEffect) }
    val SCULK_CATALYST_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(SculkCatalystEffect) }
    val BLOOD_PACT_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(BloodPactEffect) }
    val BORROWED_TIME_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(BorrowedTimeEffect) }
    val IGNITE_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(IgniteEffect) }

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
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("borrowed_time"), BorrowedTimeEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("ignite"), IgniteEffect)
    }
}