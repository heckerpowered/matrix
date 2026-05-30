/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.entity

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.common.entity.MagicLightningBolt
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random
import org.joml.Matrix4f

@Environment(EnvType.CLIENT)
class MagicLightningEntityRenderer(context: EntityRendererFactory.Context) : EntityRenderer<MagicLightningBolt>(context) {
    override fun render(lightningEntity: MagicLightningBolt, f: Float, g: Float, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, i: Int) {
        val multiplier = 5.825f
        RenderSystem.setShaderColor(multiplier, multiplier, multiplier, 1.0F)
        RenderSystem.disableBlend()
        val fs = FloatArray(8)
        val gs = FloatArray(8)
        var h = 0.0f
        var j = 0.0f
        val random = Random.create(lightningEntity.seed)

        for (k in 7 downTo 0) {
            fs[k] = h
            gs[k] = j
            h += (random.nextInt(11) - 5).toFloat()
            j += (random.nextInt(11) - 5).toFloat()
        }

        val vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLightning())
        val matrix4f = matrixStack.peek().positionMatrix

        for (l in 0..3) {
            val random2 = Random.create(lightningEntity.seed)

            for (m in 0..2) {
                var n = 7
                var o = 0
                if (m > 0) {
                    n = 7 - m
                }

                if (m > 0) {
                    o = n - 2
                }

                var p = fs[n] - h
                var q = gs[n] - j

                for (r in n downTo o) {
                    val s = p
                    val t = q
                    if (m == 0) {
                        p += (random2.nextInt(11) - 5).toFloat()
                        q += (random2.nextInt(11) - 5).toFloat()
                    } else {
                        p += (random2.nextInt(31) - 15).toFloat()
                        q += (random2.nextInt(31) - 15).toFloat()
                    }

                    var y = 0.1f + l.toFloat() * 0.2f
                    if (m == 0) {
                        y *= r.toFloat() * 0.1f + 1.0f
                    }

                    var z = 0.1f + l.toFloat() * 0.2f
                    if (m == 0) {
                        z *= (r.toFloat() - 1.0f) * 0.1f + 1.0f
                    }

                    val color = lightningEntity.lightningType.color
                    val red = color.red.toFloat() / 255F
                    val green = color.green.toFloat() / 255F
                    val blue = color.blue.toFloat() / 255F

                    drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, shiftEast1 = false, shiftSouth1 = false, shiftEast2 = true, shiftSouth2 = false)
                    drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, shiftEast1 = true, shiftSouth1 = false, shiftEast2 = true, shiftSouth2 = true)
                    drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, shiftEast1 = true, shiftSouth1 = true, shiftEast2 = false, shiftSouth2 = true)
                    drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, shiftEast1 = false, shiftSouth1 = true, shiftEast2 = false, shiftSouth2 = false)
                }
            }
        }
        // RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)
    }

    private fun drawBranch(
        matrix: Matrix4f,
        buffer: VertexConsumer,
        x1: Float,
        z1: Float,
        y: Int,
        x2: Float,
        z2: Float,
        red: Float,
        green: Float,
        blue: Float,
        offset2: Float,
        offset1: Float,
        shiftEast1: Boolean,
        shiftSouth1: Boolean,
        shiftEast2: Boolean,
        shiftSouth2: Boolean,
    ) {
        buffer.vertex(matrix, x1 + (if (shiftEast1) offset1 else -offset1), (y * 16).toFloat(), z1 + (if (shiftSouth1) offset1 else -offset1)).color(red, green, blue, 0.3f)
        buffer.vertex(matrix, x2 + (if (shiftEast1) offset2 else -offset2), ((y + 1) * 16).toFloat(), z2 + (if (shiftSouth1) offset2 else -offset2)).color(red, green, blue, 0.3f)
        buffer.vertex(matrix, x2 + (if (shiftEast2) offset2 else -offset2), ((y + 1) * 16).toFloat(), z2 + (if (shiftSouth2) offset2 else -offset2)).color(red, green, blue, 0.3f)
        buffer.vertex(matrix, x1 + (if (shiftEast2) offset1 else -offset1), (y * 16).toFloat(), z1 + (if (shiftSouth2) offset1 else -offset1)).color(red, green, blue, 0.3f)
    }

    override fun getTexture(entity: MagicLightningBolt): Identifier = PlayerScreenHandler.BLOCK_ATLAS_TEXTURE
}