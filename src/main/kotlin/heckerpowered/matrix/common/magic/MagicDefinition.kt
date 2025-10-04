/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.core.common.balance.CalculationPlan
import heckerpowered.matrix.core.common.balance.NumericCalculator
import net.minecraft.text.Text
import net.minecraft.util.Identifier
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
    open val name: Text = Text.translatable("matrix.magic.${identifier.path}.name")
    open val description: Text = Text.translatable("matrix.magic.${identifier.path}.description")

    open val uuid: UUID = UUID.nameUUIDFromBytes(identifier.toString().toByteArray())
}