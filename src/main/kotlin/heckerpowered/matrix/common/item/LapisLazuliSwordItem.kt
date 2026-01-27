/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.client.player
import heckerpowered.matrix.common.magic.ChannelExecutor
import heckerpowered.matrix.common.magic.ChannelQueue
import heckerpowered.matrix.common.magic.ChannelRequest
import heckerpowered.matrix.common.magic.DecisiveStrikeMagic
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.server.network.ServerPlayerEntity

object LapisLazuliSwordItem : SwordItem(
    lapisLazuliToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(lapisLazuliToolMaterial, 3, -2.4F))
) {
    override fun postDamageEntity(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        super.postDamageEntity(stack, target, attacker)
        if ((0..100).random() !in 0..10) {
            return
        }

        if (attacker is ServerPlayerEntity) {
            ChannelExecutor.channel(DecisiveStrikeMagic, attacker, target, ChannelRequest(costMana = false))
        } else {
            DecisiveStrikeMagic.cast(null, target, ChannelQueue(player, player.uuid, target))
        }
    }
}