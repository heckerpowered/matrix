/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import com.mojang.serialization.Codec
import heckerpowered.matrix.Matrix
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object ModComponents {
    val redstoneSuitPower = register("redstone_suit_power") { persistent(Codec.LONG) }
    val redstoneSuitMaxPower = register("redstone_suit_max_power") { persistent(Codec.LONG) }

    val maxMana = register("max_mana") { persistent(Codec.DOUBLE) }
    val infiniteMana = register("infinite_mana") { persistent(Codec.BOOL) }

    val load = register("load") { persistent(Codec.DOUBLE) }
    val maxLoad = register("max_load") { persistent(Codec.DOUBLE) }

    /**
     * @see WizardHelmet13
     */
    val accumulatedManaDelta = register("mana_consumed") { persistent(Codec.DOUBLE) }

    /**
     * Blood pact exchange rate, only used for display.
     * @see WizardHelmet13
     */
    val bloodPactExchangeRate = register("blood_pact_exchange_rate") { persistent(Codec.DOUBLE) }

    val borrowedTimeCharge = register("borrowed_time_charge") { persistent(Codec.LONG) }
    val borrowedTimeMaxCharge = register("borrowed_time_max_charge") { persistent(Codec.LONG) }
    val borrowedTimeState = register("borrowed_time_state") { persistent(Codec.BOOL) }
    val shootPerMinute = register("shoot_per_minute") { persistent(Codec.LONG) }

    val deferredDamage = register("deferred_damage") { persistent(Codec.DOUBLE) }
    val deferredDamageTick = register("deferred_damage_tick") { persistent(Codec.LONG) }

    @OptIn(ExperimentalContracts::class)
    private fun <T : Any> register(name: String, builder: DataComponentType.Builder<T>.() -> DataComponentType.Builder<T>): DataComponentType<T> {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        return Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Matrix.identifier(name),
            builder(DataComponentType.builder()).build()
        )
    }

    fun onInitialize() {
        Matrix.LOGGER.info("Registering ${Matrix.MOD_ID} components")
    }
}