/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.ui.element.DamageNumberHud
import heckerpowered.matrix.core.math.Vector3fStreamCodec
import heckerpowered.matrix.core.math.toArgb8
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

class ClientboundDamageNumberPayload(
    private val damage: Float,
    private val position: Vec3,
    private val color: Vector3f,
) : CustomPacketPayload {
    companion object {
        val payloadId = Matrix.identifier("damage_number")
        val type = CustomPacketPayload.Type<ClientboundDamageNumberPayload>(payloadId)
        val codec = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ClientboundDamageNumberPayload::damage,
            Vec3.STREAM_CODEC, ClientboundDamageNumberPayload::position,
            Vector3fStreamCodec, ClientboundDamageNumberPayload::color,
            ::ClientboundDamageNumberPayload
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    @Environment(EnvType.CLIENT)
    fun handle(context: Context) {
        DamageNumberHud.addDamageNumber(damage, color.toArgb8(), position)
    }
}