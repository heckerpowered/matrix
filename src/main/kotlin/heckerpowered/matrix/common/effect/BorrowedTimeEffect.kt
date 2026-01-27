/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.EntityTickCallback
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking
import heckerpowered.matrix.common.magic.MemoryWipeMagic.clearTargetingEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult

object BorrowedTimeEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0x5A89C0
) {
    init {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
        EntityTickCallback.EVENT.register(::onEntityTick)
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

    private fun onLivingAttack(event: DamageAccumulator): ActionResult {
        val attacker = event.attacker
        if (attacker is ServerPlayerEntity && attacker.isPhaseWalking) {
            event.target.timeUntilRegen = 0
        }

        return ActionResult.PASS
    }
}