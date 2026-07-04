/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import heckerpowered.matrix.common.entity.rule.AttributeComputationContext
import heckerpowered.matrix.common.entity.rule.AttributeComputationRule
import heckerpowered.matrix.common.entity.rule.LivingDeathContext
import heckerpowered.matrix.common.entity.rule.LivingDeathRule
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeCharge
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeMaxCharge
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeState
import heckerpowered.matrix.common.network.ClientboundBorrowedTimePayload
import heckerpowered.matrix.common.network.ClientboundTeleportPayload
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.data.language.MatrixLanguage
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipProvider
import net.minecraft.world.item.equipment.ArmorType
import java.util.function.Consumer

/**
 * Lightning Chestplate 1 'Warp Dancer'
 */
object LightningChestplate1 : Item(
    Properties().setId(ModItemIds.lightningChestplateBorrowedTime)
        .humanoidArmor(ModArmorMaterials.lightning, ArmorType.CHESTPLATE)
        .fireResistant()
        .rarity(Rarity.EPIC)
        .component(borrowedTimeCharge, 0)
        .component(borrowedTimeMaxCharge, 4000)
        .component(borrowedTimeState, false)
), TooltipProvider, AttributeComputationRule, LivingDeathRule {
    init {
        RuleRegistry.register<AttributeComputationRule>(this)
        RuleRegistry.register<LivingDeathRule>(this)
    }

    override fun onComputation(context: AttributeComputationContext) {
        val entity = context.entity
        val attribute = context.attribute
        if (attribute != Attributes.MOVEMENT_SPEED) return
        if (entity !is Player || !entity.isPhaseWalking) return

        context.multiplier += 1
    }

    override fun onLivingDeath(context: LivingDeathContext) {
        val damageSource = context.damageSource
        val attacker = damageSource.entity
        if (attacker !is Player) return

        val chestplate = attacker.getItemBySlot(EquipmentSlot.CHEST)
        if (chestplate.item !is LightningChestplate1) return

        val charge = chestplate.components.getOrDefault(borrowedTimeCharge, 0)
        val maxCharge = chestplate.components.getOrDefault(borrowedTimeMaxCharge, 4000)
        chestplate.set(borrowedTimeCharge, (charge + maxCharge / 10).coerceAtMost(maxCharge))
    }

    override fun inventoryTick(itemStack: ItemStack, level: ServerLevel, owner: Entity, slot: EquipmentSlot?) {
        super.inventoryTick(itemStack, level, owner, slot)
        if (owner !is Player) return
        if (owner.getItemBySlot(EquipmentSlot.CHEST) != itemStack) {
            itemStack.set(borrowedTimeState, false)
        }

        val charge = itemStack.components.getOrDefault(borrowedTimeCharge, 0)
        val maxCharge = itemStack.components.getOrDefault(borrowedTimeMaxCharge, 4000)
        val isActive = itemStack.components.getOrDefault(borrowedTimeState, false)
        val newCharge = if (isActive) charge - 25 else charge + 8
        itemStack.set(borrowedTimeCharge, newCharge.coerceIn(0..maxCharge))

        if (charge > 0) return
        itemStack.set(borrowedTimeState, false)

        if (owner !is ServerPlayer) return
        ServerPlayNetworking.send(owner, ClientboundBorrowedTimePayload(false))
        level.server.playerList.players.forEach {
            ServerPlayNetworking.send(it, ClientboundTeleportPayload(owner))
        }
    }

    override fun addToTooltip(context: TooltipContext, consumer: Consumer<Component>, flag: TooltipFlag, components: DataComponentGetter) {
        val charge = components.getOrDefault(borrowedTimeCharge, 0)
        val maxCharge = components.getOrDefault(borrowedTimeMaxCharge, 4000)
        val percentage = (charge.toDouble() / maxCharge.toDouble()) * 100.0
        consumer.accept(MatrixLanguage.borrowedTimeChargeDescription.copy().append("${percentage.toLong()}%"))
    }

    @JvmStatic
    val Player.isPhaseWalking: Boolean
        get() = getItemBySlot(EquipmentSlot.CHEST).components.getOrDefault(borrowedTimeState, false)

    @JvmStatic
    val Player.isBorrowedTime: Boolean
        get() = isPhaseWalking
}