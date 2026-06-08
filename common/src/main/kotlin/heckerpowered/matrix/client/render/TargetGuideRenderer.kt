/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.core.getLerpedPos
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.util.ARGB
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.sqrt

object TargetGuideRenderer {
    private const val LINE_WIDTH = 1.0F
    private val selectedTargetColor = ARGB.color(255, 25, 255, 25)
    private val candidateTargetColor = ARGB.color(255, 255, 25, 25)

    @JvmStatic
    fun onInitialize() {
        LevelRenderEvents.COLLECT_SUBMITS.register(::collectSubmits)
    }

    private fun collectSubmits(context: LevelRenderContext) {
        if (!MatrixHud.shouldRenderHud()) {
            return
        }

        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return
        val targets = MatrixHud.targetGuideChain()
        if (targets.isEmpty()) {
            return
        }

        val tickDelta = minecraft.deltaTracker.getGameTimeDeltaPartialTick(false)
        val lines = buildTargetLines(player, targets, tickDelta)
        if (lines.isEmpty()) {
            return
        }

        val cameraPosition = context.levelState().cameraRenderState.pos
        val poseStack = PoseStack()
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z)

        context.submitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.linesTranslucent()) { pose, vertexConsumer ->
            lines.forEach { line ->
                renderLine(pose, vertexConsumer, line)
            }
        }
    }

    private fun buildTargetLines(
        player: LivingEntity,
        targets: List<LivingEntity>,
        tickDelta: Float,
    ): List<TargetLine> {
        val result = mutableListOf<TargetLine>()
        var from = player.guidePosition(tickDelta)
        val selectedTarget = MatrixHud.targetedEntity

        for (target in targets) {
            val to = target.guidePosition(tickDelta)
            val color = if (target === selectedTarget) selectedTargetColor else candidateTargetColor
            result += TargetLine(from, to, color)
            from = to
        }

        return result
    }

    private fun renderLine(
        pose: PoseStack.Pose,
        vertexConsumer: VertexConsumer,
        line: TargetLine,
    ) {
        val normal = line.normal() ?: return
        vertexConsumer.addVertex(pose, line.from.x.toFloat(), line.from.y.toFloat(), line.from.z.toFloat())
            .setColor(line.color)
            .setNormal(pose, normal)
            .setLineWidth(LINE_WIDTH)
        vertexConsumer.addVertex(pose, line.to.x.toFloat(), line.to.y.toFloat(), line.to.z.toFloat())
            .setColor(line.color)
            .setNormal(pose, normal)
            .setLineWidth(LINE_WIDTH)
    }

    private fun LivingEntity.guidePosition(tickDelta: Float): Vec3 {
        return getLerpedPos(tickDelta).add(.0, boundingBox.ysize * 0.5, .0)
    }

    private data class TargetLine(
        val from: Vec3,
        val to: Vec3,
        val color: Int,
    ) {
        fun normal(): Vector3f? {
            val x = (to.x - from.x).toFloat()
            val y = (to.y - from.y).toFloat()
            val z = (to.z - from.z).toFloat()
            val length = sqrt(x * x + y * y + z * z)
            if (length <= 0.0001F) {
                return null
            }
            return Vector3f(x / length, y / length, z / length)
        }
    }
}
