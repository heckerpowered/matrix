/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.LivingDamageCallback
import heckerpowered.matrix.common.event.LivingDamageEvent
import heckerpowered.matrix.common.network.SyncHealthPayload
import heckerpowered.matrix.common.persistent.wizardHelmet
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity
import net.minecraft.world.World

object WizardHelmet10 : WizardHelmet(
    12.0,
    Settings()
        .fireproof()
        .rarity(Rarity.EPIC)
        .component(MatrixComponents.DEFERRED_DAMAGE, .0)
        .component(MatrixComponents.DEFERRED_DAMAGE_TICK, 0)
) {
    init {
        LivingDamageCallback.EVENT.register(::onLivingDamage)
    }

    fun onLivingDamage(event: LivingDamageEvent): ActionResult {
        val entity = event.entity
        if (entity !is PlayerEntity) return ActionResult.PASS
        if (entity.wizardHelmet.item !is WizardHelmet10) return ActionResult.PASS

        val stack = entity.wizardHelmet
        val deferredDamage = stack.getOrDefault(MatrixComponents.DEFERRED_DAMAGE, 0.0)
        val deferredDamageTick = stack.getOrDefault(MatrixComponents.DEFERRED_DAMAGE_TICK, 0)

        stack.set(MatrixComponents.DEFERRED_DAMAGE, deferredDamage + event.amount)
        stack.set(MatrixComponents.DEFERRED_DAMAGE_TICK, (deferredDamageTick + 20).coerceAtLeast(60))

        return ActionResult.FAIL
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        if (entity !is LivingEntity) return

        val deferredDamage = stack.getOrDefault(MatrixComponents.DEFERRED_DAMAGE, 0.0)
        val deferredDamageTick = stack.getOrDefault(MatrixComponents.DEFERRED_DAMAGE_TICK, 0)
        if (deferredDamageTick <= 0) {
            return
        }

        val damage = deferredDamage / deferredDamageTick.toDouble()
        val health = entity.health
        if (health - damage.toFloat() <= 0 && entity.isAlive) {
            entity.health -= damage.toFloat()
            entity.onDeath(entity.recentDamageSource ?: world.damageSources.genericKill())
            stack.set(MatrixComponents.DEFERRED_DAMAGE, .0)
            stack.set(MatrixComponents.DEFERRED_DAMAGE_TICK, 0)
            return
        }
        entity.health -= damage.toFloat()
        if (entity is ServerPlayerEntity) {
            ServerPlayNetworking.send(entity, SyncHealthPayload(entity))
        }

        stack.set(MatrixComponents.DEFERRED_DAMAGE, deferredDamage - damage)
        stack.set(MatrixComponents.DEFERRED_DAMAGE_TICK, deferredDamageTick - 1)
    }
}