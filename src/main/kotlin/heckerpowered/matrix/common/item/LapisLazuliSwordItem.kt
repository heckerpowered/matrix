/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.magic.DecisiveStrikeMagic
import heckerpowered.matrix.common.persistent.ChannelQueue
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
            ChannelQueue.channelMagic(DecisiveStrikeMagic, attacker, target, false)
        } else {
            DecisiveStrikeMagic.cast(null, target, ChannelQueue(target))
        }
    }
}