/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent

import heckerpowered.matrix.common.item.ModComponents
import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.magic.channel.CasterContext
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.Mana
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.rule.calculation.pipeline.CalculationPipeline
import heckerpowered.matrix.common.magic.rule.calculation.sink.ChannelQueueSizeCalculationSink
import heckerpowered.matrix.common.magic.system.ManaLedger
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

val ServerPlayer.mana: Mana
    get() = ManaLedger.mana(this)

val ServerPlayer.maxMana: Mana
    get() = wizardHelmet?.getMaxMana(this, wizardHelmetStack)?.mana ?: 0.mana

var ServerPlayer.isInfiniteMana: Boolean
    get() = wizardHelmetStack.getOrDefault(ModComponents.infiniteMana, false)
    set(value) {
        wizardHelmetStack[ModComponents.infiniteMana] = value
    }

val Player.isWizard: Boolean
    get() = getItemBySlot(EquipmentSlot.HEAD).item is WizardHelmet

val Player.wizardHelmetStack: ItemStack
    get() = getItemBySlot(EquipmentSlot.HEAD)

val Player.wizardHelmet: WizardHelmet?
    get() = wizardHelmetStack.item as? WizardHelmet

val Player.queueSize: Long
    get() {
        val sink = ChannelQueueSizeCalculationSink()
        val context = MagicCalculationContext(CasterContext.fromEntity(this))
        CalculationPipeline.apply(context, sink)
        return sink.queueSize
    }