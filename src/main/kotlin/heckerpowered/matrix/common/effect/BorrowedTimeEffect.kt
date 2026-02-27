/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.DamageOutcomeContext
import heckerpowered.matrix.common.combat.damage.DamageOutcomeRule
import heckerpowered.matrix.common.combat.damage.attacker
import heckerpowered.matrix.common.event.EntityTickCallback
import heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking
import heckerpowered.matrix.common.magic.spell.MemoryWipeMagic.clearTargetingEntity
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object BorrowedTimeEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0x5A89C0
), DamageOutcomeRule {
    init {
        EntityTickCallback.EVENT.register(::onEntityTick)
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    private fun onEntityTick(entity: LivingEntity) {
        if (entity !is MobEntity) {
            return
        }

        val target = entity.target ?: return
        if (target !is PlayerEntity || !target.isPhaseWalking) {
            return
        }

        entity.clearTargetingEntity()
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        val attacker = context.attacker as? ServerPlayerEntity ?: return
        if (!attacker.isPhaseWalking) return

        context.target.timeUntilRegen = 0
    }
}