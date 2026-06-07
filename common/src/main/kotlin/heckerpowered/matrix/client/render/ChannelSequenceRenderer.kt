/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.ui.foundation.animation.AnimationClock
import heckerpowered.matrix.client.ui.foundation.animation.DoubleAnimation
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.common.magic.spell.SculkCatalystMagic
import heckerpowered.matrix.core.getLerpedPos
import heckerpowered.matrix.core.toDegrees
import heckerpowered.matrix.core.wrapDegrees
import heckerpowered.matrix.client.render.effect.SculkCatalystEffectRenderer
import heckerpowered.matrix.client.render.post.CollapseEffectRenderer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.entity.LivingEntity
import org.joml.Vector2f
import java.time.Duration
import java.util.WeakHashMap
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.tan

object ChannelSequenceRenderer {
    private val easingFunction = ElasticEase().also {
        it.easingMode = EasingMode.OUT
        it.oscillations = 0
    }

    class OffsetAnimation {
        val xOffsetAnimationClock = AnimationClock(Duration.ofMillis(300), .0, -24.0)
        var xOffsetAnimation = DoubleAnimation(xOffsetAnimationClock, easingFunction)
    }

    val channelSequenceAnimationMap = WeakHashMap<LivingEntity, MutableList<ChannelAnimation>>()
    val offsetAnimationMap = WeakHashMap<LivingEntity, OffsetAnimation>()

    fun onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register {
            channelSequenceAnimationMap.keys.toList().forEach(::onEntityTick)
        }
        HudElementRegistry.addLast(Matrix.identifier("channel_sequence")) { drawContext, tickCounter ->
            onHudRender(drawContext, tickCounter)
        }
    }

    private fun onHudRender(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        for ((entity, animation) in channelSequenceAnimationMap) {
            renderEntityChannelSequence(drawContext, tickCounter, entity, animation)
        }
    }

    private fun renderEntityChannelSequence(
        drawContext: GuiGraphicsExtractor,
        tickCounter: DeltaTracker,
        entity: LivingEntity,
        animation: List<ChannelAnimation>,
    ) {
        val tickDelta = tickCounter.getGameTimeDeltaPartialTick(false)
        val lerpedPosition = entity.getLerpedPos(tickDelta).add(.0, entity.boundingBox.ysize + 0.25, .0)
        val entityScreenPosition = lerpedPosition.toScreenPosition(tickDelta, drawContext.guiWidth(), drawContext.guiHeight()) ?: return

        animation.forEachIndexed { index, channelAnimation ->
            val alpha = channelAnimation.opacityAnimation.animatedValue
            val backgroundColor = Color(128, 0, 0, (128 * alpha).toInt()).toInt()
            val progressColor = Color(255, 0, 0, (255 * alpha).toInt()).toInt()
            val animatedX = offsetAnimationMap[entity]?.xOffsetAnimation?.animatedValue ?: .0

            val isChanneling = index == 0 || animation[index - 1].currentChannelTime >= animation[index - 1].channelTime
            val partialProgress = if (isChanneling) tickDelta else .0f
            val channelProgress = (
                (channelAnimation.currentChannelTime + partialProgress - channelAnimation.initialProgressOffset) /
                    channelAnimation.channelTime.toDouble()
                ).coerceAtMost(1.0)

            val minX = (entityScreenPosition.x - 8.0 + index * 24 + animatedX).toInt()
            val maxX = (entityScreenPosition.x + 8.0 + index * 24 + animatedX).toInt()
            val topY = (entityScreenPosition.y + channelAnimation.shownAnimation.animatedValue).toInt()
            val bottomY = (entityScreenPosition.y + 16.0 + channelAnimation.shownAnimation.animatedValue).toInt()
            val progressY = (entityScreenPosition.y + (1 - channelProgress).coerceIn(.0, 1.0) * 16.0 + channelAnimation.shownAnimation.animatedValue).toInt()

            drawContext.fill(minX, topY, maxX, progressY, backgroundColor)
            drawContext.fill(minX, progressY, maxX, bottomY, progressColor)

            if (channelAnimation.magic == SculkCatalystMagic && channelAnimation.currentChannelTime < channelAnimation.channelTime) {
                SculkCatalystEffectRenderer.entity = entity
                CollapseEffectRenderer.dissolveFactor.value = channelProgress / 4.0
            }
        }
    }

    private fun net.minecraft.world.phys.Vec3.toScreenPosition(tickDelta: Float, viewportWidth: Int, viewportHeight: Int): Vector2f? {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return null
        val eyePosition = player.getEyePosition(tickDelta)
        val direction = subtract(eyePosition)
        val distance2D = sqrt(direction.x * direction.x + direction.z * direction.z)
        if (distance2D <= 0.0001 && abs(direction.y) <= 0.0001) {
            return null
        }

        val pitch = -toDegrees(atan2(direction.y, distance2D))
        val yaw = toDegrees(atan2(direction.z, direction.x)) - 90.0
        val yawDifference = wrapDegrees(yaw - player.getYRot(tickDelta).toDouble())
        val pitchDifference = wrapDegrees(pitch - player.getXRot(tickDelta).toDouble())

        val verticalFov = minecraft.options.fov().get().toDouble().coerceIn(30.0, 110.0)
        val aspectRatio = viewportWidth.toDouble() / viewportHeight.toDouble().coerceAtLeast(1.0)
        val horizontalFov = toDegrees(2.0 * atan2(tan(java.lang.Math.toRadians(verticalFov) / 2.0) * aspectRatio, 1.0))

        val x = viewportWidth / 2.0 +
            tan(java.lang.Math.toRadians(yawDifference)) / tan(java.lang.Math.toRadians(horizontalFov) / 2.0) * viewportWidth / 2.0
        val y = viewportHeight / 2.0 +
            tan(java.lang.Math.toRadians(pitchDifference)) / tan(java.lang.Math.toRadians(verticalFov) / 2.0) * viewportHeight / 2.0

        return Vector2f(
            x.coerceIn(0.0, viewportWidth.toDouble()).toFloat(),
            y.coerceIn(0.0, viewportHeight.toDouble()).toFloat(),
        )
    }

    private fun onEntityTick(entity: LivingEntity) {
        if (!entity.level().isClientSide) {
            return
        }
        val channelAnimation = channelSequenceAnimationMap[entity] ?: return
        val removed = channelAnimation.removeIf { it.currentChannelTime > it.channelTime && it.opacityAnimation.animatedValue == 0.0 }
        if (removed) {
            offsetAnimationMap[entity]?.xOffsetAnimationClock?.apply {
                from = .0
                to = .0
            }
        }
        if (channelAnimation.isEmpty()) {
            channelSequenceAnimationMap.remove(entity)
            offsetAnimationMap.remove(entity)
            return
        }
        channelAnimation
            .firstOrNull { it.currentChannelTime <= it.channelTime }
            ?.tick(entity)
    }
}
