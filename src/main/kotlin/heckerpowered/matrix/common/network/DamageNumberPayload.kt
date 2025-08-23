/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.client.ui.element.DamageNumberHud
import heckerpowered.matrix.common.event.LivingDamageCallback
import heckerpowered.matrix.common.event.LivingDamageEvent
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.tag.DamageTypeTags
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f

class DamageNumberPayload(
    private val damage: Float,
    private val position: Vec3d,
    private val color: Vector3f,
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<DamageNumberPayload> = CustomPayload.id("damage_number")
        val codec: PacketCodec<PacketByteBuf, DamageNumberPayload> =
            PacketCodec.of(DamageNumberPayload::encode) { buffer ->
                val damage = buffer.readFloat()

                val x = buffer.readDouble()
                val y = buffer.readDouble()
                val z = buffer.readDouble()

                val r = buffer.readFloat()
                val g = buffer.readFloat()
                val b = buffer.readFloat()

                val position = Vec3d(x, y, z)
                val color = Vector3f(r, g, b)
                DamageNumberPayload(damage, position, color)
            }

        init {
            LivingDamageCallback.EVENT.register(::onLivingDamage)
        }

        fun onLivingDamage(event: LivingDamageEvent): ActionResult {
            if (event.entity.world !is ServerWorld) {
                return ActionResult.PASS
            }
            val position = event.entity.boundingBox.center
            val color = getColorForDamageSource(event.damageSource)
            val payload = DamageNumberPayload(event.amount, position, color)
            event.entity.world.server?.playerManager?.playerList?.forEach { player ->
                ServerPlayNetworking.send(player, payload)
            }
            return ActionResult.PASS
        }

        fun getColorForDamageSource(damage: DamageSource): Vector3f {
            return when {
                damage.isOf(DamageTypes.MAGIC) -> Vector3f(25.0F, 25.0F, 128.0F)
                damage.isOf(MatrixDamageTypes.magic) -> Vector3f(25.0F, 128.0F, 255.0F)
                damage.isIn(DamageTypeTags.IS_FIRE) -> Vector3f(255.0F, 100.0F, 25.0F)
                // damage.isOf(DamageTypes.) -> Vector3f(25.0F, 25.0F, 128.0F)
                else -> Vector3f(255.0F, 255.0F, 255.0F)
            }
        }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeFloat(damage)
        buffer.writeDouble(position.x)
        buffer.writeDouble(position.y)
        buffer.writeDouble(position.z)
        buffer.writeFloat(color.x)
        buffer.writeFloat(color.y)
        buffer.writeFloat(color.z)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return DamageNumberPayload.id
    }

    @Environment(EnvType.CLIENT)
    fun handle(context: Context) {
        DamageNumberHud.addDamageNumber(damage, color, position)
    }
}