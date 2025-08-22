/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.magics.DecisiveStrikeMagic
import heckerpowered.matrix.common.persistent.ChannelSequence
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
            ChannelSequence.channelMagic(DecisiveStrikeMagic, attacker, target, false)
        } else {
            DecisiveStrikeMagic.cast(null, target, ChannelSequence(target))
        }
    }
}