/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.render.effect.SculkCatalystEffectRenderer
import heckerpowered.matrix.client.render.post.CollapseEffectRenderer
import heckerpowered.matrix.client.ui.foundation.animation.AnimationClock
import heckerpowered.matrix.client.ui.foundation.animation.DoubleAnimation
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.common.event.EntityTickCallback
import heckerpowered.matrix.common.magic.spell.SculkCatalystMagic
import heckerpowered.matrix.core.MatrixLivingEntity
import heckerpowered.matrix.core.worldToScreen
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EntityAttachmentType
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.MathHelper
import org.joml.Matrix4f
import java.time.Duration
import java.util.*

class ChannelSequenceRenderer(
    context: FeatureRendererContext<LivingEntity, EntityModel<LivingEntity>>,
) : FeatureRenderer<LivingEntity, EntityModel<LivingEntity>>(context) {

    private val disabled = false

    companion object {
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

        init {
            EntityTickCallback.EVENT.register(::onEntityTick)
            // HudRenderCallback.EVENT.register(::onHudRender)
            // ClientTickEvents.START_WORLD_TICK.register { world ->
            //     for (entry in channelSequenceAnimationMap) {
            //         if (entry.key.world != world) {
            //             continue
            //         }
            //         for (channelAnimation in entry.value) {
            //             if (channelAnimation.currentChannelTime <= channelAnimation.channelTime) {
            //                 channelAnimation.tick(entry.key)
            //                 break
            //             }
            //         }
            //     }

            //     for (entry in channelSequenceAnimationMap.entries) {
            //         if (entry.key.world != world) {
            //             continue
            //         }
            //         val entity = entry.key
            //         val list = entry.value
            //         val iterator = list.iterator()
            //         while (iterator.hasNext()) {
            //             val channelAnimation = iterator.next()
            //             if (channelAnimation.currentChannelTime > channelAnimation.channelTime && channelAnimation.opacityAnimation.animatedValue == 0.0) {
            //                 offsetAnimationMap[entity]?.let {
            //                     it.xOffsetAnimationClock.from = .0
            //                     it.xOffsetAnimationClock.to = .0
            //                 }
            //                 iterator.remove()
            //             }
            //         }
            //     }
            // }
        }

        private fun onHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
            for (entry in channelSequenceAnimationMap) {
                val entity = entry.key
                val animation = entry.value
                renderEntityChannelSequence(drawContext, tickCounter, entity, animation)
            }
        }

        private fun renderRectangle(
            buffer: BufferBuilder,
            positionMatrix: Matrix4f,
            rectangle: Rectangle,
            color: Color,
            z: Float = 0F,
        ) {
            buffer.vertex(positionMatrix, rectangle.min.x.toFloat(), rectangle.min.y.toFloat(), z).color(color.toInt())
            buffer.vertex(positionMatrix, rectangle.max.x.toFloat(), rectangle.min.y.toFloat(), z).color(color.toInt())
            buffer.vertex(positionMatrix, rectangle.max.x.toFloat(), rectangle.max.y.toFloat(), z).color(color.toInt())
            buffer.vertex(positionMatrix, rectangle.min.x.toFloat(), rectangle.max.y.toFloat(), z).color(color.toInt())
        }

        private fun renderEntityChannelSequence(drawContext: DrawContext, tickCounter: RenderTickCounter, entity: LivingEntity, animation: List<ChannelAnimation>) {
            val tickDelta = tickCounter.getTickDelta(false)

            val transformationMatrix = drawContext.matrices.peek().positionMatrix
            val tessellator = Tessellator.getInstance()
            val buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

            val lerpedPosition = entity.getLerpedPos(tickDelta).add(
                .0,
                entity.boundingBox.lengthY,
                .0
            )
            val entityScreenPosition = worldToScreen(lerpedPosition) ?: return

            animation.forEachIndexed { index, channelAnimation ->
                val color = Color(128, 0, 0, (128 * channelAnimation.opacityAnimation.animatedValue).toInt())
                val progressColor = Color(255, 0, 0, (255 * channelAnimation.opacityAnimation.animatedValue).toInt())
                val animatedX = if (channelAnimation.opacityAnimation.animatedValue != 1.0 && channelAnimation.opacityAnimationClock.to == 0.0) {
                    offsetAnimationMap[entity]?.xOffsetAnimation?.animatedValue ?: 0.0
                } else {
                    offsetAnimationMap[entity]?.xOffsetAnimation?.animatedValue ?: 0.0
                }

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

                val progressRectangle = Rectangle(
                    Point(
                        entityScreenPosition.x + -8.0 + index * 24 + animatedX,
                        entityScreenPosition.y + 16.0 + channelAnimation.shownAnimation.animatedValue
                    ), Point(
                        entityScreenPosition.x + 8.0 + index * 24 + animatedX,
                        entityScreenPosition.y + (1 - channelProgress) * 16.0 + channelAnimation.shownAnimation.animatedValue
                    )
                )
                val rectangle = Rectangle(
                    Point(
                        entityScreenPosition.x + -8.0 + index * 24 + animatedX,
                        entityScreenPosition.y + (1 - channelProgress).coerceIn(.0..1.0) * 16.0 + channelAnimation.shownAnimation.animatedValue
                    ), Point(
                        entityScreenPosition.x + 8.0 + index * 24 + animatedX,
                        entityScreenPosition.y + .0 + channelAnimation.shownAnimation.animatedValue
                    )
                )
                renderRectangle(buffer, transformationMatrix, rectangle, color, 0F)
                renderRectangle(buffer, transformationMatrix, progressRectangle, progressColor, 0F)
            }

            RenderSystem.enableBlend()
            RenderSystem.setShader(GameRenderer::getPositionColorProgram)
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

            buffer.endNullable()?.let { BufferRenderer.draw(it) }

            RenderSystem.disableBlend()
        }

        private fun onEntityTick(entity: LivingEntity) {
            if (!entity.world.isClient) {
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

    override fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        entity: LivingEntity,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float,
    ) {
        if (entity !is MatrixLivingEntity || disabled) {
            return
        }

        val channelAnimations = channelSequenceAnimationMap[entity] ?: return

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

        val minecraftClient = MinecraftClient.getInstance()
        val camera = minecraftClient.gameRenderer.camera
        val entityRenderer = minecraftClient.entityRenderDispatcher.getRenderer(entity)
        val positionOffset = entityRenderer.getPositionOffset(entity, tickDelta)

        val entityPartialX = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderX, entity.x)
        val entityPartialY = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderY, entity.y)
        val entityPartialZ = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderZ, entity.z)

        val entityRelativeX = entityPartialX - camera.pos.x
        val entityRelativeY = entityPartialY - camera.pos.y
        val entityRelativeZ = entityPartialZ - camera.pos.z

        val offsetX = entityRelativeX + positionOffset.x
        val offsetY = entityRelativeY + positionOffset.y
        val offsetZ = entityRelativeZ + positionOffset.z

        val matrixStack = MatrixStack()
        matrixStack.push()
        matrixStack.translate(offsetX, offsetY, offsetZ)

        val position = entity.attachments.getPointNullable(EntityAttachmentType.NAME_TAG, 0, entity.getYaw(tickDelta))!!

        matrixStack.push()
        matrixStack.translate(position.x, position.y + 0.5, position.z)
        matrixStack.multiply(minecraftClient.entityRenderDispatcher.rotation)
        matrixStack.scale(0.025f, -0.025f, 0.025f)

        val positionMatrix = matrixStack.peek().positionMatrix

        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
        RenderSystem.enableBlend()
        val layer = RenderLayer.getGui()
        val buffer = vertexConsumers.getBuffer(layer)

        channelAnimations.forEachIndexed { index, channelAnimation ->
            val color = Color(128, 0, 0, (128 * channelAnimation.opacityAnimation.animatedValue).toInt())
            val progressColor = Color(255, 0, 0, (255 * channelAnimation.opacityAnimation.animatedValue).toInt())
            val animatedX = if (channelAnimation.opacityAnimation.animatedValue != 1.0 && channelAnimation.opacityAnimationClock.to == 0.0) {
                offsetAnimationMap[entity]?.xOffsetAnimation?.animatedValue ?: 0.0
            } else {
                offsetAnimationMap[entity]?.xOffsetAnimation?.animatedValue ?: 0.0
            }

            val isChanneling = if (index == 0) {
                true
            } else {
                channelAnimations[index - 1].let { it.currentChannelTime >= it.channelTime }
            }
            val partialProgress = if (isChanneling) {
                tickDelta
            } else {
                .0f
            }
            val channelProgress = ((channelAnimation.currentChannelTime + partialProgress - channelAnimation.initialProgressOffset) / channelAnimation.channelTime.toDouble()).coerceAtMost(1.0)
            val progressRectangle = Rectangle(
                Point(
                    -8.0 + index * 24 + animatedX, 16.0 + channelAnimation.shownAnimation.animatedValue
                ), Point(
                    8.0 + index * 24 + animatedX, (1 - channelProgress) * 16.0 + channelAnimation.shownAnimation.animatedValue
                )
            )
            val rectangle = Rectangle(
                Point(
                    -8.0 + index * 24 + animatedX, (1 - channelProgress).coerceIn(.0..1.0) * 16.0 + channelAnimation.shownAnimation.animatedValue
                ), Point(
                    8.0 + index * 24 + animatedX, .0 + channelAnimation.shownAnimation.animatedValue
                )
            )
            renderRectangle(
                buffer, positionMatrix, rectangle, color, light, if (isChanneling) {
                    0.01f
                } else {
                    -0.01f
                }
            )
            renderRectangle(
                buffer, positionMatrix, progressRectangle, progressColor, light, if (isChanneling) {
                    0f
                } else {
                    -0.01f
                }
            )
            if (channelAnimations[index].magic == SculkCatalystMagic &&
                channelAnimations[index].currentChannelTime < channelAnimations[index].channelTime
            ) {
                SculkCatalystEffectRenderer.entity = entity
                CollapseEffectRenderer.dissolveFactor.value = channelProgress / 4.0
            }
        }

        RenderSystem.disableBlend()
        matrixStack.pop()
        matrixStack.pop()
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)
    }

    private fun renderRectangle(
        buffer: VertexConsumer,
        positionMatrix: Matrix4f?,
        rectangle: Rectangle,
        color: Color,
        light: Int,
        z: Float = 0.01f,
    ) {
        buffer.vertex(positionMatrix, rectangle.min.x.toFloat(), rectangle.min.y.toFloat(), z).color(color.toInt())
        buffer.vertex(positionMatrix, rectangle.max.x.toFloat(), rectangle.min.y.toFloat(), z).color(color.toInt())
        buffer.vertex(positionMatrix, rectangle.max.x.toFloat(), rectangle.max.y.toFloat(), z).color(color.toInt())
        buffer.vertex(positionMatrix, rectangle.min.x.toFloat(), rectangle.max.y.toFloat(), z).color(color.toInt())
    }
}