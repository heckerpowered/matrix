/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.system

import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.ChannelTimeCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.CostCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.MagicCalculationSink
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

object PlayerOverclockState : MagicCalculationContributor {
    const val MIN_RATE = 1.0
    const val MAX_RATE = 10.0

    private data class Rates(
        val mana: Double = MIN_RATE,
        val magic: Double = MIN_RATE,
    )

    private val rates = mutableMapOf<UUID, Rates>()

    fun onInitialize() {
        RuleRegistry.register<MagicCalculationContributor>(this)
    }

    fun set(player: ServerPlayer, mana: Double, magic: Double) {
        rates[player.uuid] = Rates(
            mana = mana.clampRate(),
            magic = magic.clampRate(),
        )
    }

    fun mana(player: ServerPlayer): Double {
        return rates[player.uuid]?.mana ?: MIN_RATE
    }

    fun magic(player: ServerPlayer): Double {
        return rates[player.uuid]?.magic ?: MIN_RATE
    }

    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        val player = context.playerOrNull() as? ServerPlayer ?: return
        when (sink) {
            is CostCalculationSink -> sink.costMultiplier *= mana(player)
            is ChannelTimeCalculationSink -> sink.channelSpeedBonus += magic(player) - MIN_RATE
        }
    }

    private fun Double.clampRate(): Double {
        return coerceIn(MIN_RATE, MAX_RATE)
    }
}
