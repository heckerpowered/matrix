/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.ModMobEffects.BLOOD_PACT_EFFECT
import heckerpowered.matrix.common.enchantment.ModEnchantments.bloodPact
import heckerpowered.matrix.common.enchantment.getEnchantmentLevel
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import io.netty.buffer.ByteBuf
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffectInstance

data object ServerboundActivateBloodPactPayload : CustomPacketPayload {
    val payloadId = Matrix.identifier("active_blood_pact")
    val type = CustomPacketPayload.Type<ServerboundActivateBloodPactPayload>(payloadId)
    val codec = StreamCodec.unit<ByteBuf, ServerboundActivateBloodPactPayload>(this)

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(@Suppress("unused") payload: ServerboundActivateBloodPactPayload, context: Context) {
        val player = context.player()
        val level = player.level()
        if (player.wizardHelmetStack.getEnchantmentLevel(level, bloodPact) <= 0) return
        if (player.hasEffect(BLOOD_PACT_EFFECT) || player.cooldowns.isOnCooldown(player.wizardHelmetStack)) {
            level.playSound(null, player.x, player.y, player.z, SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 3.0F, 1.0F)
            return
        }

        player.addEffect(MobEffectInstance(BLOOD_PACT_EFFECT, 20 * 30, 0, false, true))
        level.playSound(null, player.x, player.y, player.z, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0F, 1.0F)
        player.cooldowns.addCooldown(player.wizardHelmetStack, 20 * (30 + 14)) // 30 = duration, 14 = cooldown

        player.wizardHelmet?.onBloodPactActive(player, player.wizardHelmetStack)
    }
}