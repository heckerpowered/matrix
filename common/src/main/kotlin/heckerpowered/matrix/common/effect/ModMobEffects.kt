/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.Matrix
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.effect.MobEffect

object ModMobEffects {
    val ManaOverload = register("mana_overload", ManaOverloadEffect)
    val ArmorPenetration = register("armor_penetration", ArmorPenetrationEffect)
    val CrippleMovement = register("cripple_movement", CrippleMovementEffect)
    val WitherArmorCharged = register("wither_armor_charged", WitherArmorChargedEffect)
    val WitherArmor = register("wither_armor", WitherArmorEffect)
    val Angered = register("angered", AngeredEffect)
    val Exposed = register("exposed", ExposedEffect)
    val BloodPact = register("blood_pact", BloodPactEffect)
    val BorrowedTime = register("borrowed_time_effect", BorrowedTimeEffect)
    val Ignite = register("ignite", IgniteEffect)
    val Absolvrift = register("absolvrift", AbsolvriftEffect)
    val HealthShrink = register("health_shrink", HealthShrinkEffect)

    private fun register(name: String, effect: MobEffect): Holder<MobEffect> {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Matrix.identifier(name), effect)
    }

    fun onInitialize() {
    }
}