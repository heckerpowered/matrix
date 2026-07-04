/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import heckerpowered.matrix.common.combat.damage.DamageSettlementContext
import heckerpowered.matrix.common.combat.damage.DamageSettlementRule
import heckerpowered.matrix.common.network.ClientboundSyncHealthPayload
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity

object WizardHelmet10 : WizardHelmet(
    Properties().setId(ModItemIds.wizardHelmet10)
        .fireResistant()
        .rarity(Rarity.EPIC)
        .maxMana(12.0)
        .component(ModComponents.deferredDamage, .0)
        .component(ModComponents.deferredDamageTick, 0)
), DamageSettlementRule {
    init {
        RuleRegistry.register<DamageSettlementRule>(this)
    }

    override fun inventoryTick(itemStack: ItemStack, level: ServerLevel, owner: Entity, slot: EquipmentSlot?) {
        super.inventoryTick(itemStack, level, owner, slot)
        if (owner !is LivingEntity) return

        val deferredDamage = itemStack[ModComponents.deferredDamage] ?: .0
        val deferredDamageTick = itemStack[ModComponents.deferredDamageTick] ?: 0
        if (deferredDamageTick <= 0) return

        val damage = deferredDamage / deferredDamageTick.toDouble()
        val health = owner.health
        if (health - damage.toFloat() <= 0 && owner.isAlive) {
            owner.health -= damage.toFloat()
            owner.die(owner.lastDamageSource ?: level.damageSources().genericKill())
            itemStack[ModComponents.deferredDamage] = .0
            itemStack[ModComponents.deferredDamageTick] = 0
            return
        }

        owner.health -= damage.toFloat()
        if (owner is ServerPlayer) {
            ServerPlayNetworking.send(owner, ClientboundSyncHealthPayload(owner))
        }

        itemStack[ModComponents.deferredDamage] = deferredDamage - damage
        itemStack[ModComponents.deferredDamageTick] = deferredDamageTick - 1
    }

    override fun onSettlement(context: DamageSettlementContext) {
        val entity = context.target
        if (entity !is Player) return
        if (entity.wizardHelmetStack.item !is WizardHelmet10) return

        val stack = entity.wizardHelmetStack
        val deferredDamage = stack.getOrDefault(ModComponents.deferredDamage, 0.0)
        val deferredDamageTick = stack.getOrDefault(ModComponents.deferredDamageTick, 0)

        stack[ModComponents.deferredDamage] = deferredDamage + context.remainingDamage
        stack[ModComponents.deferredDamageTick] = (deferredDamageTick + 20).coerceAtLeast(60)

        context.consume(context.remainingDamage)
    }
}