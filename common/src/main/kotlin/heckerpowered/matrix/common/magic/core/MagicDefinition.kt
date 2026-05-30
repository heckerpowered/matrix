/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.core

import heckerpowered.matrix.common.magic.resource.Mana
import heckerpowered.matrix.common.magic.system.GameTick
import heckerpowered.matrix.core.common.balance.CalculationPlan
import heckerpowered.matrix.core.common.balance.NumericCalculator
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.Enchantment
import java.util.*

/**
 * Immutable metadata that defines a magic's identity and baseline values.
 *
 * A [MagicDefinition] carries only descriptive or static information:
 * - Unique identifier
 * - Display name and description. (for UI / localization)
 * - Baseline numeric values (mana cost, channel time)
 *
 * This type is data-only. It does not perform any calculations or hold runtime state.
 * Behavior and modifiers are applied externally (e.g. by a [CalculationPlan] and [NumericCalculator])
 *
 * @property identifier unique key of this magic. Serves as the stable identity across network,
 * persistence, and registries.
 * @property name display name of this magic.
 * @property description description text shown in the UI.
 * @property baseCost the baseline mana cost before any modifiers are applied.
 * @property baseChannelTime the baseline channeling time in ticks before any modifiers are applied.
 */
open class MagicDefinition(
    val identifier: Identifier,
    val baseCost: Mana,
    val baseChannelTime: GameTick,
) {
    open val name: MutableComponent = Component.translatable("matrix.magic.${identifier.path}.name")
    open val description: MutableComponent = Component.translatable("matrix.magic.${identifier.path}.description")

    open val uuid: UUID = identifier.asMagicUuid()
}

/**
 * Converts this identifier to the UUID representation used by the Magic protocol.
 *
 * The result is deterministic and is used as the stable magic identity across
 * registries, networking, and persistence.
 */
fun Identifier.asMagicUuid(): UUID {
    return UUID.nameUUIDFromBytes(toString().toByteArray(Charsets.UTF_8))
}

fun ResourceKey<Enchantment>.asMagicUuid(): UUID {
    return identifier().asMagicUuid()
}