/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.common.effect.MatrixStatusEffects.BLOOD_PACT_EFFECT
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.BLOOD_PACT_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.getEnchantmentLevel
import heckerpowered.matrix.common.persistent.wizardHelmet
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

class ActiveBloodPactPayload : CustomPayload {
    companion object {
        val id: CustomPayload.Id<ActiveBloodPactPayload> = CustomPayload.id("active_blood_pact")
        val codec: PacketCodec<PacketByteBuf, ActiveBloodPactPayload> =
            PacketCodec.of(ActiveBloodPactPayload::encode) {
                ActiveBloodPactPayload()
            }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun encode(buffer: PacketByteBuf) {
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ActiveBloodPactPayload.id
    }

    fun handle(context: Context) {
        val player = context.player()
        if (player.wizardHelmet.getEnchantmentLevel(BLOOD_PACT_ENCHANTMENT_KEY) <= 0) {
            return
        }

        if (player.hasStatusEffect(BLOOD_PACT_EFFECT) || player.itemCooldownManager.isCoolingDown(player.wizardHelmet.item)) {
            player.serverWorld.playSound(null, player.x, player.y, player.z, SoundEvents.ENTITY_BLAZE_HURT, SoundCategory.PLAYERS, 3.0F, 1.0F)
            return
        }

        player.addStatusEffect(StatusEffectInstance(BLOOD_PACT_EFFECT, 20 * 30, 0, false, true))
        player.serverWorld.playSound(null, player.x, player.y, player.z, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1.0F, 1.0F)
        player.itemCooldownManager.set(player.wizardHelmet.item, 20 * (30 + 14)) // 30 = duration, 14 = cooldown
    }
}