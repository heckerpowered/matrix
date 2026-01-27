/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.Matrix
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry

object MatrixStatusEffects {
    @JvmStatic
    @get:JvmName("getManaOverloadEffect")
    val MANA_OVERLOAD_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(ManaOverloadEffect) }

    @JvmStatic
    @get:JvmName("getArmorPenetrationEffect")
    val ARMOR_PENETRATION_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(ArmorPenetrationEffect) }

    @JvmStatic
    @get:JvmName("getCrippleMovementEffect")
    val CRIPPLE_MOVEMENT_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(CrippleMovementEffect) }

    @JvmStatic
    @get:JvmName("getWitherArmorChargedEffect")
    val WITHER_ARMOR_CHARGED_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(WitherArmorChargedEffect) }

    @JvmStatic
    @get:JvmName("getWitherArmorEffect")
    val WITHER_ARMOR_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(WitherArmorEffect) }

    @JvmStatic
    @get:JvmName("getAngeredEffect")
    val ANGERED_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(AngeredEffect) }

    @JvmStatic
    @get:JvmName("getExposedEffect")
    val EXPOSED_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(ExposedEffect) }

    @JvmStatic
    @get:JvmName("getBloodPactEffect")
    val BLOOD_PACT_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(BloodPactEffect) }

    @JvmStatic
    @get:JvmName("getBorrowedTimeEffect")
    val BORROWED_TIME_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(BorrowedTimeEffect) }

    @JvmStatic
    @get:JvmName("getIgniteEffect")
    val IGNITE_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(IgniteEffect) }

    @JvmStatic
    @get:JvmName("getAbsolvriftEffect")
    val ABSOLVRIFT_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(AbsolvriftEffect) }

    @JvmStatic
    @get:JvmName("getHealthShrinkEffect")
    val HEALTH_SHRINK_EFFECT: RegistryEntry<StatusEffect> by lazy { Registries.STATUS_EFFECT.getEntry(HealthShrinkEffect) }

    fun onInitialize() {
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("mana_overload"), ManaOverloadEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("armor_penetration"), ArmorPenetrationEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("cripple_movement"), CrippleMovementEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("wither_armor_charged"), WitherArmorChargedEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("wither_armor"), WitherArmorEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("angered"), AngeredEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("exposed"), ExposedEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("blood_pact"), BloodPactEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("borrowed_time"), BorrowedTimeEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("ignite"), IgniteEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("absolvrift"), AbsolvriftEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("health_shrink"), HealthShrinkEffect)
    }
}