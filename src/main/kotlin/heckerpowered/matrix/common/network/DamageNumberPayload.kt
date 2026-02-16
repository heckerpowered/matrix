/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.client.ui.element.DamageNumberHud
import heckerpowered.matrix.common.event.LivingDamageCallback
import heckerpowered.matrix.common.event.LivingDamageEvent
import heckerpowered.matrix.common.tag.MatrixDamageTypeTags
import heckerpowered.matrix.core.extension.BoxExtension.sample
import heckerpowered.matrix.core.math.Vector3fExtensions.toArgb8
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
            val position = event.entity.boundingBox.sample(event.amount.toDouble(), 0.1)
            val color = getColorForDamageSource(event.damageSource)
            val payload = DamageNumberPayload(event.amount, position, color)
            event.entity.world.server?.playerManager?.playerList?.forEach { player ->
                ServerPlayNetworking.send(player, payload)
            }
            return ActionResult.PASS
        }

        fun getColorForDamageSource(damage: DamageSource): Vector3f {
            return when {
                damage.isIn(MatrixDamageTypeTags.magic) ->
                    Vector3f(25f, 128f, 255f)

                damage.isOf(DamageTypes.MAGIC) ||
                        damage.isOf(DamageTypes.INDIRECT_MAGIC) ->
                    Vector3f(25f, 25f, 128f)

                damage.isIn(DamageTypeTags.IS_FIRE) ||
                        damage.isOf(DamageTypes.LAVA) ||
                        damage.isOf(DamageTypes.HOT_FLOOR) ->
                    Vector3f(255f, 100f, 25f)

                damage.isOf(DamageTypes.EXPLOSION) ||
                        damage.isOf(DamageTypes.PLAYER_EXPLOSION) ||
                        damage.isOf(DamageTypes.FIREWORKS) ->
                    Vector3f(255f, 170f, 60f)

                damage.isOf(DamageTypes.LIGHTNING_BOLT) ||
                        damage.isOf(DamageTypes.SONIC_BOOM) ||
                        damage.isOf(DamageTypes.WIND_CHARGE) ->
                    Vector3f(160f, 80f, 255f)

                damage.isOf(DamageTypes.STARVE) ||
                        damage.isOf(DamageTypes.DRY_OUT) ->
                    Vector3f(255f, 230f, 80f)

                damage.isOf(DamageTypes.THORNS) ||
                        damage.isOf(DamageTypes.CACTUS) ||
                        damage.isOf(DamageTypes.SWEET_BERRY_BUSH) ->
                    Vector3f(80f, 200f, 80f)

                else ->
                    Vector3f(255f, 255f, 255f)
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
        DamageNumberHud.addDamageNumber(damage, color.toArgb8(), position)
    }
}