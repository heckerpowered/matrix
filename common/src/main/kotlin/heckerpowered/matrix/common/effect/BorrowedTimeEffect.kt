/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.DamageOutcomeContext
import heckerpowered.matrix.common.combat.damage.DamageOutcomeRule
import heckerpowered.matrix.common.combat.damage.attacker
import heckerpowered.matrix.common.entity.rule.EntityUpdateContext
import heckerpowered.matrix.common.entity.rule.EntityUpdateRule
import heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking
import heckerpowered.matrix.common.magic.spell.MemoryWipeMagic.clearTargetingEntity
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player

object BorrowedTimeEffect : MobEffect(
    MobEffectCategory.BENEFICIAL,
    0x5A89C0
), EntityUpdateRule, DamageOutcomeRule {
    init {
        RuleRegistry.register<EntityUpdateRule>(this)
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    override fun onUpdate(context: EntityUpdateContext) {
        val entity = context.entity
        if (entity !is Mob) return
        val target = entity.target ?: return
        if (target !is Player || !target.isPhaseWalking) return

        entity.clearTargetingEntity()
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        val attacker = context.attacker as? ServerPlayer ?: return
        if (!attacker.isPhaseWalking) return

        context.target.invulnerableTime = 0
    }
}