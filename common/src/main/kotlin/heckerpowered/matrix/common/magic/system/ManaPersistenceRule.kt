/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.system

import heckerpowered.matrix.common.entity.rule.LivingLoadContext
import heckerpowered.matrix.common.entity.rule.LivingPersistenceRule
import heckerpowered.matrix.common.entity.rule.LivingSaveContext
import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.server.level.ServerPlayer

object ManaPersistenceRule : LivingPersistenceRule {
    private const val MANA_KEY = "matrix_mana"

    init {
        RuleRegistry.register<LivingPersistenceRule>(this)
    }

    fun onInitialize() {
    }

    override fun save(context: LivingSaveContext) {
        val player = context.entity as? ServerPlayer ?: return
        val mana = ManaLedger.mana(player).toDouble()
        if (mana.isFinite()) {
            context.output.putDouble(MANA_KEY, mana)
        }
    }

    override fun load(context: LivingLoadContext) {
        val player = context.entity as? ServerPlayer ?: return
        val mana = context.input.getDoubleOr(MANA_KEY, .0)
        if (!mana.isFinite()) {
            return
        }

        WizardHelmet.syncManaBounds(player)
        ManaLedger.setMana(player, mana.mana)
    }
}
