/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.entity

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.common.entity.MagicLightningBolt
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.util.RandomSource
import org.joml.Matrix4f

/**
 * Render state for [MagicLightningBolt]: mirrors vanilla's
 * `net.minecraft.client.renderer.entity.state.LightningBoltRenderState` (which only carries
 * `seed`) but also captures [color], since unlike vanilla lightning, ours renders in one of
 * several [MagicLightningBolt.LightningType] colors.
 */
@Environment(EnvType.CLIENT)
class MagicLightningRenderState : EntityRenderState() {
    var seed: Long = 0L
    var color: Color = MagicLightningBolt.LightningType.NORMAL.color
}

@Environment(EnvType.CLIENT)
class MagicLightningEntityRenderer(context: EntityRendererProvider.Context) :
    EntityRenderer<MagicLightningBolt, MagicLightningRenderState>(context) {

    override fun createRenderState(): MagicLightningRenderState = MagicLightningRenderState()

    override fun extractRenderState(entity: MagicLightningBolt, state: MagicLightningRenderState, partialTick: Float) {
        super.extractRenderState(entity, state, partialTick)
        state.seed = entity.seed
        state.color = entity.lightningType.color
    }

    override fun submit(
        state: MagicLightningRenderState,
        matrixStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        cameraRenderState: CameraRenderState,
    ) {
        submitNodeCollector.submitCustomGeometry(matrixStack, RenderTypes.lightning()) { pose, vertexConsumer ->
            renderLightning(state, pose.pose(), vertexConsumer)
        }
    }

    private fun renderLightning(state: MagicLightningRenderState, matrix4f: Matrix4f, vertexConsumer: VertexConsumer) {
        // 26.2 note: RenderSystem.setShaderColor no longer applies a global tint to
        // submitCustomGeometry draws (pipeline-based rendering has no equivalent hook), so the
        // brightness multiplier that used to be applied via setShaderColor is baked directly
        // into the per-vertex color instead. Vertex color is 8-bit (no HDR headroom like the
        // old shader uniform had), so the multiplied result is clamped to 1.0 rather than
        // wrapping; visually this reads as "fully bright" for channels the multiplier pushes
        // past white, matching the old glow's intent on an additive RenderTypes.lightning()
        // blend even though the exact float value differs from the unclamped GL uniform.
        val brightnessMultiplier = 5.825f
        val fs = FloatArray(8)
        val gs = FloatArray(8)
        var h = 0.0f
        var j = 0.0f
        val random = RandomSource.create(state.seed)

        for (k in 7 downTo 0) {
            fs[k] = h
            gs[k] = j
            h += (random.nextInt(11) - 5).toFloat()
            j += (random.nextInt(11) - 5).toFloat()
        }

        for (l in 0..3) {
            val random2 = RandomSource.create(state.seed)

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

                    val color = state.color
                    val red = (color.red.toFloat() / 255F * brightnessMultiplier).coerceIn(0f, 1f)
                    val green = (color.green.toFloat() / 255F * brightnessMultiplier).coerceIn(0f, 1f)
                    val blue = (color.blue.toFloat() / 255F * brightnessMultiplier).coerceIn(0f, 1f)

                    drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, shiftEast1 = false, shiftSouth1 = false, shiftEast2 = true, shiftSouth2 = false)
                    drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, shiftEast1 = true, shiftSouth1 = false, shiftEast2 = true, shiftSouth2 = true)
                    drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, shiftEast1 = true, shiftSouth1 = true, shiftEast2 = false, shiftSouth2 = true)
                    drawBranch(matrix4f, vertexConsumer, p, q, r, s, t, red, green, blue, y, z, shiftEast1 = false, shiftSouth1 = true, shiftEast2 = false, shiftSouth2 = false)
                }
            }
        }
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
        buffer.addVertex(matrix, x1 + (if (shiftEast1) offset1 else -offset1), (y * 16).toFloat(), z1 + (if (shiftSouth1) offset1 else -offset1)).setColor(red, green, blue, 0.3f)
        buffer.addVertex(matrix, x2 + (if (shiftEast1) offset2 else -offset2), ((y + 1) * 16).toFloat(), z2 + (if (shiftSouth1) offset2 else -offset2)).setColor(red, green, blue, 0.3f)
        buffer.addVertex(matrix, x2 + (if (shiftEast2) offset2 else -offset2), ((y + 1) * 16).toFloat(), z2 + (if (shiftSouth2) offset2 else -offset2)).setColor(red, green, blue, 0.3f)
        buffer.addVertex(matrix, x1 + (if (shiftEast2) offset1 else -offset1), (y * 16).toFloat(), z1 + (if (shiftSouth2) offset1 else -offset1)).setColor(red, green, blue, 0.3f)
    }
}
