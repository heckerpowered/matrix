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
import heckerpowered.matrix.core.worldToScreen
import heckerpowered.matrix.extension.MatrixLivingEntity
import heckerpowered.matrix.client.render.effect.SculkCatalystEffectRenderer
import heckerpowered.matrix.client.render.post.CollapseEffectRenderer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.entity.LivingEntity
import java.time.Duration
import java.util.WeakHashMap

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
        val lerpedPosition = entity.getLerpedPos(tickDelta).add(.0, entity.boundingBox.ysize, .0)
        val entityScreenPosition = worldToScreen(lerpedPosition) ?: return

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

    private fun onEntityTick(entity: LivingEntity) {
        if (!entity.level().isClientSide || entity !is MatrixLivingEntity) {
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
        channelAnimation
            .firstOrNull { it.currentChannelTime <= it.channelTime }
            ?.tick(entity)
    }
}
