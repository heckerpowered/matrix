/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.client.render.particle.system.ExplosionParticle
import heckerpowered.matrix.client.render.post.CameraShake
import heckerpowered.matrix.client.render.post.ShockwaveRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import org.joml.Vector2f
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ExplosionPayload(
    val entityId: Int,
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<ExplosionPayload> = CustomPayload.id("explosion")
        val codec: PacketCodec<PacketByteBuf, ExplosionPayload> =
            PacketCodec.of(ExplosionPayload::encode) { buffer ->
                ExplosionPayload(
                    buffer.readInt()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(entityId)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ExplosionPayload.id
    }

    @Environment(EnvType.CLIENT)
    fun handle(context: Context) {
        val client = context.client()!!
        val world = client.world ?: return
        val entity = world.getEntityById(entityId) ?: return

        ShockwaveRenderer.wavePosition = entity.pos.toVector3f()

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
        ExplosionParticle.spawnParticleAt(entity.pos)
    }
}