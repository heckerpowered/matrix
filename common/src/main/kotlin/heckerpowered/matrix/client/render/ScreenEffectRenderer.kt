/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.event.PostProcessCallback
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.render.effect.SculkCatalystEffectRenderer
import heckerpowered.matrix.client.render.post.CollapseEffectRenderer
import heckerpowered.matrix.client.render.post.ShockwaveRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.ui.foundation.animation.ColorAnimation
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.effect.ModMobEffects.ANGERED_EFFECT
import heckerpowered.matrix.common.effect.ModMobEffects.WITHER_ARMOR_EFFECT
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.core.particles.ParticleTypes
import org.joml.Vector4f
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER
import java.time.Duration
import kotlin.math.abs
import kotlin.random.Random
import net.minecraft.world.phys.Vec3

object ScreenEffectRenderer {
    private var previousAngryState = false
    private var previousWitherArmorState = false
    private var previousWitherArmorDuration = 0
    private var initialized = false

    private val colorAnimation = ColorAnimation(
        red = SimpleDoubleAnimation(from = 1.0, to = 1.0, initValue = 1.0, duration = Duration.ofMillis(1000)),
        green = SimpleDoubleAnimation(from = 1.0, to = 1.0, initValue = 1.0, duration = Duration.ofMillis(1000)),
        blue = SimpleDoubleAnimation(from = 1.0, to = 1.0, initValue = 1.0, duration = Duration.ofMillis(1000)),
    )
    private val edgeThresholdAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(1000))
    val bloomThresholdAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(1000))

    private val colorFilterShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/color_filter.fsh", GL_FRAGMENT_SHADER),
        )
    }

    private val edgeHighlightShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/edge_highlight.fsh", GL_FRAGMENT_SHADER),
        )
    }

    fun onInitialize() {
        if (initialized) {
            return
        }
        initialized = true
        ClientTickEvents.START_CLIENT_TICK.register(::onTick)
        PostProcessCallback.EVENT.register(::onPostProcess)

        colorAnimation.red.start()
        colorAnimation.green.start()
        colorAnimation.blue.start()
        edgeThresholdAnimation.start()
        bloomThresholdAnimation.start()
    }

    fun beginRenderEntity() {
        // Entity post effects are currently driven by the 26.2 post-process and tick paths.
    }

    fun endRenderEntity() {
        if (SculkCatalystEffectRenderer.entity?.isAlive == false) {
            SculkCatalystEffectRenderer.entity = null
        }
    }

    fun spawnParticleAt(position: Vec3, count: Int = 1) {
        val level = minecraft.level ?: return
        repeat(count.coerceAtLeast(0)) {
            val velocity = randomVelocity(0.045)
            level.addParticle(
                ParticleTypes.WITCH,
                position.x,
                position.y,
                position.z,
                velocity.x,
                velocity.y,
                velocity.z,
            )
        }
    }

    fun onWitherArmorEffectApplied() {
        val player = player ?: return
        previousWitherArmorState = true
        previousWitherArmorDuration = player.getEffect(WITHER_ARMOR_EFFECT)?.duration ?: 0

        colorAnimation.red.from = 2.0
        colorAnimation.red.to = if (previousAngryState) 2.0 else 1.0
        colorAnimation.red.duration = Duration.ofMillis(10000)
        colorAnimation.red.start()

        edgeThresholdAnimation.from = .0
        edgeThresholdAnimation.to = 1.0
        edgeThresholdAnimation.duration = Duration.ofMillis(10000)
        edgeThresholdAnimation.start()

        bloomThresholdAnimation.from = -1.0
        bloomThresholdAnimation.to = 1.0
        bloomThresholdAnimation.duration = Duration.ofMillis(1500)
        bloomThresholdAnimation.start()

        CollapseEffectRenderer.dissolveFactor.from = 1.0
        CollapseEffectRenderer.dissolveFactor.to = .0
        CollapseEffectRenderer.dissolveFactor.duration = Duration.ofMillis(1000)
        CollapseEffectRenderer.dissolveFactor.start()

        ShockwaveRenderer.wavePosition = player.position().toVector3f()
        ShockwaveRenderer.waveRadius.from = .0
        ShockwaveRenderer.waveRadius.to = 128.0
        ShockwaveRenderer.waveRadius.duration = Duration.ofMillis(1000)
        ShockwaveRenderer.waveRadius.start()

        ShockwaveRenderer.waveSize.from = 1.0
        ShockwaveRenderer.waveSize.to = .0
        ShockwaveRenderer.waveSize.duration = Duration.ofMillis(1000)
        ShockwaveRenderer.waveSize.start()

        applyPostProcessState()
    }

    private fun onTick(minecraft: Minecraft) {
        val player = minecraft.player ?: run {
            previousAngryState = false
            previousWitherArmorState = false
            resetPostProcessState()
            return
        }

        val angry = player.hasEffect(ANGERED_EFFECT)
        if (!angry && previousAngryState) {
            onAngeredEffectRemoved()
        } else if (angry && !previousAngryState) {
            onAngeredEffectApplied()
        }
        previousAngryState = angry

        val witherArmor = player.getEffect(WITHER_ARMOR_EFFECT)
        val witherArmorDuration = witherArmor?.duration ?: 0
        if (witherArmor == null && previousWitherArmorState) {
            previousWitherArmorState = false
        } else if (witherArmor != null && (!previousWitherArmorState || witherArmorDuration > previousWitherArmorDuration)) {
            onWitherArmorEffectApplied()
        }
        previousWitherArmorDuration = witherArmorDuration

        if (SculkCatalystEffectRenderer.entity != null) {
            SculkCatalystEffectRenderer.render()
            edgeThresholdAnimation.value = edgeThresholdAnimation.animatedValue.coerceAtMost(0.45)
        }

        applyPostProcessState()
    }

    private fun onPostProcess() {
        if (!CollapseEffectRenderer.dissolveFactor.isAnimating &&
            CollapseEffectRenderer.dissolveFactor.animatedValue <= 0.001
        ) {
            return
        }

        val source = PostProcessRenderer.sourceFramebuffer
        val output = PostProcessRenderer.currentFramebuffer()
        PostProcessRenderer.renderShaderToFramebuffer(
            CollapseEffectRenderer.shader,
            output,
            mapOf("framebuffer" to source, "depthAttachment" to source),
        )
        PostProcessRenderer.copyFramebuffer(output, source)
    }

    private fun onAngeredEffectApplied() {
        colorAnimation.red.value = 2.0
        colorAnimation.red.duration = Duration.ofMillis(1000)
        colorAnimation.green.value = 1.0
        colorAnimation.green.duration = Duration.ofMillis(1000)
        colorAnimation.blue.value = 1.0
        edgeThresholdAnimation.value = 0.3
    }

    private fun onAngeredEffectRemoved() {
        colorAnimation.red.value = 1.0
        colorAnimation.green.value = 1.0
        colorAnimation.blue.value = 1.0
        edgeThresholdAnimation.value = 1.0
    }

    private fun applyPostProcessState() {
        MatrixPostUniforms.colorFilterColor = Vector4f(
            colorAnimation.red.animatedValue.toFloat(),
            colorAnimation.green.animatedValue.toFloat(),
            colorAnimation.blue.animatedValue.toFloat(),
            1.0F,
        )
        MatrixPostUniforms.edgeHighlightThreshold = edgeThresholdAnimation.animatedValue.toFloat()
        MatrixPostUniforms.edgeHighlightColor = Vector4f(0.7F, 0.1F, 0.1F, 1.0F)

        if (isColorFilterActive()) {
            PostProcessRenderer.postProcessShaders.add(colorFilterShader)
        } else {
            PostProcessRenderer.postProcessShaders.remove(colorFilterShader)
        }

        if (isEdgeHighlightActive()) {
            PostProcessRenderer.postProcessShaders.add(edgeHighlightShader)
        } else {
            PostProcessRenderer.postProcessShaders.remove(edgeHighlightShader)
        }
    }

    private fun resetPostProcessState() {
        colorAnimation.red.animatedValue = 1.0
        colorAnimation.green.animatedValue = 1.0
        colorAnimation.blue.animatedValue = 1.0
        edgeThresholdAnimation.animatedValue = 1.0
        applyPostProcessState()
    }

    private fun isColorFilterActive(): Boolean {
        return colorAnimation.red.isAnimating ||
            colorAnimation.green.isAnimating ||
            colorAnimation.blue.isAnimating ||
            abs(colorAnimation.red.animatedValue - 1.0) > 0.001 ||
            abs(colorAnimation.green.animatedValue - 1.0) > 0.001 ||
            abs(colorAnimation.blue.animatedValue - 1.0) > 0.001
    }

    private fun isEdgeHighlightActive(): Boolean {
        return edgeThresholdAnimation.isAnimating || edgeThresholdAnimation.animatedValue < 0.999
    }

    private fun randomVelocity(scale: Double): Vec3 {
        return Vec3(
            (Random.nextDouble() - 0.5) * scale,
            Random.nextDouble() * scale,
            (Random.nextDouble() - 0.5) * scale,
        )
    }
}
