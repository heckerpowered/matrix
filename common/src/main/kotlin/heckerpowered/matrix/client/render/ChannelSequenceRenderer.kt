/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.render.effect.SculkCatalystEffectRenderer
import heckerpowered.matrix.client.render.post.CollapseEffectRenderer
import heckerpowered.matrix.client.ui.foundation.animation.AnimationClock
import heckerpowered.matrix.client.ui.foundation.animation.DoubleAnimation
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.common.magic.spell.SculkCatalystMagic
import heckerpowered.matrix.core.getLerpedPos
import heckerpowered.matrix.core.worldToScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.entity.LivingEntity
import java.time.Duration
import java.util.WeakHashMap

/**
 * Renders per-entity channel-sequence progress bars above an entity's head.
 *
 * 26.2 note (structural decision): in 1.21 this was a [net.minecraft.client.render.entity.feature.FeatureRenderer]
 * (yarn) / [net.minecraft.client.renderer.entity.layers.RenderLayer] (mojmap) billboarding 3D quads
 * in the entity's local space via [heckerpowered.matrix.client.MatrixClient]'s
 * `LivingEntityFeatureRendererRegistrationCallback` registration. On 26.2 that registration
 * point (`LivingEntityRenderLayerRegistrationCallback`) only ever hands a
 * [net.minecraft.client.renderer.entity.state.LivingEntityRenderState], which carries no
 * reference back to the source [LivingEntity] (nor a stable id) and there is no Fabric API
 * extraction hook to stamp one on via [net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState]
 * for arbitrary (including vanilla) living entities without a Mixin into
 * `LivingEntityRenderer.extractRenderState` — Mixins are out of scope for this port pass.
 *
 * `channelSequenceAnimationMap`/`offsetAnimationMap` are keyed by the live [LivingEntity]
 * already (see [heckerpowered.matrix.client.render.ChannelAnimation]), so this renderer is
 * rewritten as a screen-space HUD overlay (`HudElementRegistry`) that iterates those maps
 * directly instead of going through the entity-render-state pipeline. [worldToScreen] performs
 * the same perspective projection the old 3D billboard implicitly did, so the on-screen result
 * (position, size, animation) is unchanged; only the rendering mechanism moved from a 3D quad
 * (positioned via the entity attachment + camera-facing rotation matrix) to an equivalent 2D
 * screen-space rectangle. No visual feature was dropped.
 */
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
        for (entry in channelSequenceAnimationMap) {
            val entity = entry.key
            val animation = entry.value
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

        val lerpedPosition = entity.getLerpedPos(tickDelta).add(
            .0,
            entity.boundingBox.ysize,
            .0
        )
        val entityScreenPosition = worldToScreen(
            lerpedPosition,
            viewportWidth = drawContext.guiWidth(),
            viewportHeight = drawContext.guiHeight(),
        ) ?: return

        animation.forEachIndexed { index, channelAnimation ->
            val color = Color(128, 0, 0, (128 * channelAnimation.opacityAnimation.animatedValue).toInt())
            val progressColor = Color(255, 0, 0, (255 * channelAnimation.opacityAnimation.animatedValue).toInt())
            val animatedX = offsetAnimationMap[entity]?.xOffsetAnimation?.animatedValue ?: 0.0

            val isChanneling = if (index == 0) {
                true
            } else {
                animation[index - 1].let { it.currentChannelTime >= it.channelTime }
            }
            val partialProgress = if (isChanneling) {
                tickDelta
            } else {
                .0f
            }
            val channelProgress = ((channelAnimation.currentChannelTime + partialProgress - channelAnimation.initialProgressOffset) / channelAnimation.channelTime.toDouble()).coerceAtMost(1.0)

            val minX = (entityScreenPosition.x + -8.0 + index * 24 + animatedX).toInt()
            val maxX = (entityScreenPosition.x + 8.0 + index * 24 + animatedX).toInt()
            val topY = (entityScreenPosition.y + 16.0 + channelAnimation.shownAnimation.animatedValue).toInt()
            val progressY = (entityScreenPosition.y + (1 - channelProgress) * 16.0 + channelAnimation.shownAnimation.animatedValue).toInt()
            val bottomY = (entityScreenPosition.y + (1 - channelProgress).coerceIn(.0, 1.0) * 16.0 + channelAnimation.shownAnimation.animatedValue).toInt()
            val topOfBar = (entityScreenPosition.y + .0 + channelAnimation.shownAnimation.animatedValue).toInt()

            // Background (unfilled) bar: from the animated top down to the progress line.
            drawContext.fill(minX, bottomY, maxX, topOfBar, color.toInt())
            // Progress (filled) bar: from the fixed bottom up to the progress line.
            drawContext.fill(minX, topY, maxX, progressY, progressColor.toInt())

            if (channelAnimation.magic == SculkCatalystMagic &&
                channelAnimation.currentChannelTime < channelAnimation.channelTime
            ) {
                SculkCatalystEffectRenderer.entity = entity
                CollapseEffectRenderer.dissolveFactor.value = channelProgress / 4.0
            }
        }
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
        channelAnimation
            .firstOrNull { it.currentChannelTime <= it.channelTime }
            ?.tick(entity)
    }
}
