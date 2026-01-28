/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.magic.channel.ChannelAttempt
import heckerpowered.matrix.common.magic.channel.ChannelExecutor
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.spell.DecisiveStrikeMagic
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
            val attempt = ChannelAttempt(costMana = false)
            val invocation = MagicInvocation.fromEntity(attacker, target)
            ChannelExecutor.channel(DecisiveStrikeMagic, invocation, attempt)
        } else {
            // TODO: Support living casters
            // DecisiveStrikeMagic.cast()
        }
    }
}