/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import heckerpowered.matrix.common.magic.channel.ChannelExecutor
import heckerpowered.matrix.common.magic.channel.ExecutionPolicy
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.spell.DecisiveStrikeMagic
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

object LapisLazuliSwordItem : Item(
    Properties().setId(ModItemIds.lapisLazuliSword).sword(ModToolMaterials.lapisLazuli, 3.0F, -2.4F)
) {
    override fun postHurtEnemy(itemStack: ItemStack, mob: LivingEntity, attacker: LivingEntity) {
        super.postHurtEnemy(itemStack, mob, attacker)
        if ((1..100).random() !in 1..10) {
            return
        }

        val attacker = attacker as? Player ?: return
        val attempt = ExecutionPolicy(costMana = false)
        val invocation = MagicInvocation.fromEntity(attacker, mob)
        ChannelExecutor.channel(DecisiveStrikeMagic, invocation, attempt)
    }
}