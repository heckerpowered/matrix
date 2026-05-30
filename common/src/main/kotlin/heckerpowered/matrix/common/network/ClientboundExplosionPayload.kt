/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.render.particle.system.ExplosionParticle
import heckerpowered.matrix.client.render.post.CameraShake
import heckerpowered.matrix.client.render.post.ShockwaveRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import org.joml.Vector2f
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ClientboundExplosionPayload(
    val entityId: Int,
) : CustomPacketPayload {
    companion object {
        val payloadId = Matrix.identifier("explosion")
        val type = CustomPacketPayload.Type<ClientboundExplosionPayload>(payloadId)
        val codec = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundExplosionPayload::entityId, ::ClientboundExplosionPayload
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    @Environment(EnvType.CLIENT)
    fun handle(context: Context) {
        val client = context.client()
        val world = client.level ?: return
        val entity = world.getEntity(entityId) ?: return

        ShockwaveRenderer.wavePosition = entity.position().toVector3f()

        ShockwaveRenderer.waveRadius.from = .0
        ShockwaveRenderer.waveRadius.to = 16.0
        ShockwaveRenderer.waveRadius.duration = Duration.ofMillis(1000)
        ShockwaveRenderer.waveRadius.start()

        ShockwaveRenderer.waveSize.from = 1.0
        ShockwaveRenderer.waveSize.to = .0
        ShockwaveRenderer.waveSize.duration = Duration.ofMillis(1000)
        ShockwaveRenderer.waveSize.start()

        CameraShake.shake(strength = 1F, duration = 100.milliseconds)

        ExplosionParticle.randomVelocityModule.speedRange = Vector2f(0.0F, 20.0F)
        ExplosionParticle.spawnParticleAt(entity.position())
    }
}