/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.event.PostProcessCallback
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.render.effect.SculkCatalystEffectRenderer
import heckerpowered.matrix.client.render.particle.ParticleSystem
import heckerpowered.matrix.client.render.particle.memory.MemoryLayout
import heckerpowered.matrix.client.render.particle.module.particle_render.ParticleSpriteRendererModule
import heckerpowered.matrix.client.render.particle.module.particle_spawn.InitializeParticleModule
import heckerpowered.matrix.client.render.particle.module.particle_spawn.RandomLifetimeModule
import heckerpowered.matrix.client.render.particle.module.particle_spawn.RandomVelocityModule
import heckerpowered.matrix.client.render.particle.module.particle_update.DragModule
import heckerpowered.matrix.client.render.particle.module.particle_update.KillParticleModule
import heckerpowered.matrix.client.render.particle.module.particle_update.ParticleStateModule
import heckerpowered.matrix.client.render.particle.module.particle_update.ScaleSpriteSizeBySpeedModule
import heckerpowered.matrix.client.render.particle.system.ExplosionParticle
import heckerpowered.matrix.client.render.post.BloomEffect
import heckerpowered.matrix.client.render.post.CollapseEffectRenderer
import heckerpowered.matrix.client.render.post.ShockwaveRenderer
import heckerpowered.matrix.client.render.shader.RadialBlurRenderer.samples
import heckerpowered.matrix.client.render.shader.VortexRenderer
import heckerpowered.matrix.client.render.state.*
import heckerpowered.matrix.client.render.state.capabilities.BlendState
import heckerpowered.matrix.client.render.state.capabilities.DepthTestState
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.DissolveShader
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.client.ui.foundation.animation.ColorAnimation
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.effect.MatrixStatusEffects.ANGERED_EFFECT
import heckerpowered.matrix.common.effect.MatrixStatusEffects.WITHER_ARMOR_EFFECT
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.magic.spell.SculkCatalystMagic
import heckerpowered.matrix.core.approximatelyEqual
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL46.*
import java.time.Duration

object ScreenEffectRenderer {

    private var previousAngryState = false

    private var previousWitherArmorState = false
    private var previousWitherArmorDuration = 0L

    private val colorAnimation = ColorAnimation(
        red = SimpleDoubleAnimation(from = 1.0, to = 1.0, initValue = 1.0, duration = Duration.ofMillis(1000)),
        green = SimpleDoubleAnimation(from = 1.0, to = 1.0, initValue = 1.0, duration = Duration.ofMillis(1000)),
        blue = SimpleDoubleAnimation(from = 1.0, to = 1.0, initValue = 1.0, duration = Duration.ofMillis(1000))
    )
    private val ghostStrengthAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))
    private val auraAlphaAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))

    private val auraShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/aura.fsh", GL_FRAGMENT_SHADER),
            uniforms = arrayOf(
                UniformProvider("entityDepthAttachment") { pointer ->
                    glActiveTexture(GlConst.GL_TEXTURE0)
                    glBindTexture(GlConst.GL_TEXTURE_2D, sceneFramebuffer.depthAttachment)
                    RenderSystem.glUniform1i(pointer, 0)
                },
                UniformProvider("entityColorAttachment") { pointer ->
                    glActiveTexture(GlConst.GL_TEXTURE0 + 1)
                    glBindTexture(GlConst.GL_TEXTURE_2D, sceneFramebuffer.colorAttachment)
                    RenderSystem.glUniform1i(pointer, 1)
                },
                UniformProvider("sceneDepthAttachment") { pointer ->
                    glActiveTexture(GlConst.GL_TEXTURE0 + 2)
                    glBindTexture(GlConst.GL_TEXTURE_2D, minecraft.framebuffer.depthAttachment)
                    glUniform1i(pointer, 2)
                },
                UniformProvider("sceneColorAttachment") { pointer ->
                    glActiveTexture(GlConst.GL_TEXTURE0 + 3)
                    glBindTexture(GlConst.GL_TEXTURE_2D, minecraft.framebuffer.colorAttachment)
                    glUniform1i(pointer, 3)
                },
                UniformProvider("noiseColorAttachment") { pointer ->
                    glActiveTexture(GlConst.GL_TEXTURE0 + 4)
                    glBindTexture(GlConst.GL_TEXTURE_2D, DissolveShader.perlinNoiseTextureId)
                    glUniform1i(pointer, 4)
                },
                UniformProvider("time") { pointer ->
                    val age = minecraft.player?.age?.toFloat() ?: 0F
                    val deltaTime = minecraft.renderTickCounter.tickDelta
                    val time = age + deltaTime
                    glUniform1f(pointer, time / 1000.0F)
                },
                UniformProvider("alpha") { pointer ->
                    glUniform1f(pointer, auraAlphaAnimation.animatedValue.toFloat())
                },
                UniformProvider("auraColor") { pointer ->
                    val color = colorAnimation
                    glUniform4f(
                        pointer,
                        color.red.animatedValue.toFloat() / color.red.to.toFloat(),
                        color.red.animatedValue.toFloat() / color.red.to.toFloat(),
                        color.red.animatedValue.toFloat() / color.red.to.toFloat(),
                        color.red.animatedValue.toFloat() / color.red.to.toFloat()
                    )
                }
            )
        )
    }

    private val blurRadiusAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))

    private val edgeThresholdAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))
    val bloomThresholdAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))

    private val colorFilterShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/color_filter.fsh", GL_FRAGMENT_SHADER),
            uniforms = arrayOf(
                PostProcessRenderer.framebufferProvider,
                UniformProvider("color") { pointer ->
                    glUniform4f(
                        pointer,
                        colorAnimation.red.animatedValue.toFloat(),
                        colorAnimation.green.animatedValue.toFloat(),
                        colorAnimation.blue.animatedValue.toFloat(),
                        1.0F
                    )
                }
            )
        )
    }

    private val edgeHighlightShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/edge_highlight.fsh", GL_FRAGMENT_SHADER),
            uniforms = arrayOf(
                PostProcessRenderer.framebufferProvider,
                UniformProvider("edgeThreshold") { pointer ->
                    glUniform1f(pointer, edgeThresholdAnimation.animatedValue.toFloat())
                },
                UniformProvider("edgeColor") { pointer ->
                    glUniform4f(pointer, 0.7F, 0.1F, 0.1F, 1.0F)
                }
            )
        )
    }

    private val radialBlurShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/blur/radial_blur.fsh", GL_FRAGMENT_SHADER),
            uniforms = arrayOf(
                PostProcessRenderer.framebufferProvider,
                UniformProvider("strength") { pointer ->
                    glUniform1f(pointer, ghostStrengthAnimation.animatedValue.toFloat())
                },
                UniformProvider("samples") { pointer ->
                    glUniform1i(pointer, samples)
                }
            )
        )
    }

    fun onInitialize() {
        ClientTickEvents.START_CLIENT_TICK.register(::onTick)
        PostProcessCallback.EVENT.register(::onPostProcess)

        colorAnimation.red.start()
        colorAnimation.green.start()
        colorAnimation.blue.start()

        edgeThresholdAnimation.start()
        edgeThresholdAnimation.animatedValue = 1.0
        bloomThresholdAnimation.animatedValue = 1.0
    }

    private fun shouldRenderBloom(): Boolean {
        return true
        return !bloomThresholdAnimation.animatedValue.approximatelyEqual(1.0) ||
                CollapseEffectRenderer.dissolveFactor.animatedValue != .0 ||
                ShockwaveRenderer.waveSize.animatedValue != .0
    }

    private fun onPostProcess() {
        StateIsolation.isolate(
            FramebufferState(minecraft.framebuffer), ViewportState(minecraft.framebuffer),
            BlendState(true), BlendFuncSeparateState(GL_ONE, GL_ONE)
        ) {
            VortexRenderer.render()
        }
        bloomThresholdAnimation.animatedValue = 1.0
        if (!shouldRenderBloom()) {
            return
        }

        BloomEffect.brightnessThreshold = bloomThresholdAnimation.animatedValue.toFloat() + 0.1F
        BloomEffect.brightnessPassFramebuffer = minecraft.framebuffer
        BloomEffect.renderBloom()

        StateIsolation.isolate(
            FramebufferState.captureSnapshot(), ViewportState.captureSnapshot(),
            BlendState(true), BlendFuncSeparateState(GL_ONE, GL_ONE)
        ) {
            PostProcessRenderer.copyFramebuffer(BloomEffect.bloomUpFramebuffer, minecraft.framebuffer, false)
        }

        // ToneMapping.exposureLinear = 1.0f
        // ToneMapping.exposureEv = 0.0f
        // ToneMapping.render(minecraft.framebuffer, toneMapFramebuffer)
        // StateIsolation.isolate(
        //     FramebufferState(minecraft.framebuffer),
        //     ViewportState(minecraft.framebuffer)
        // ) {
        //     PostProcessRenderer.copyFramebuffer(toneMapFramebuffer, minecraft.framebuffer, false)
        // }
    }

    val particleSystem by lazy {
        ParticleSystem(
            10000,
            particleSpawnModules = arrayOf(
                InitializeParticleModule(),
                RandomVelocityModule(),
                RandomLifetimeModule(),
            ),
            particleUpdateModules = arrayOf(
                KillParticleModule(),
                ParticleStateModule(),
                DragModule(),
                ScaleSpriteSizeBySpeedModule()
            ),
            particleRenderModules = arrayOf(
                ParticleSpriteRendererModule()
            ),
            MemoryLayout.DEFAULT_LAYOUT
        )
    }

    fun spawnParticleAt(position: Vec3d, count: Int = 1) {
        val particleState = (particleSystem.particleSpawnModules.first { it is InitializeParticleModule } as InitializeParticleModule).particleState
        particleState.x = position.x.toFloat()
        particleState.y = position.y.toFloat()
        particleState.z = position.z.toFloat()

        val multiplier = 4F

        particleState.colorR = 1.0F * multiplier
        particleState.colorG = 0.5F * multiplier
        particleState.colorB = 1.0F * multiplier
        particleState.colorA = 1.0F

        particleState.spriteSize = 80.0F
        particleState.scale = 1F

        index += count
        if (index > particleSystem.particleCount) {
            index = 0
        }
        particleSystem.spawnPartialParticles(index, count)
    }

    var index: Int = 0

    fun onTick(minecraftClient: MinecraftClient) {
        if (minecraftClient.player == null) {
            return
        }
        if (player.getStatusEffect(ANGERED_EFFECT) == null && previousAngryState) {
            onAngeredEffectRemoved()
            previousAngryState = false
        } else if (player.getStatusEffect(ANGERED_EFFECT) != null && !previousAngryState) {
            onAngeredEffectApplied()
            previousAngryState = true
        }

        val witherArmorStatusEffect = player.getStatusEffect(WITHER_ARMOR_EFFECT)
        if (witherArmorStatusEffect == null && previousWitherArmorState) {
            previousWitherArmorState = false
        } else if (witherArmorStatusEffect != null && !previousWitherArmorState ||
            (witherArmorStatusEffect?.duration?.toLong() ?: 0L) > previousWitherArmorDuration
        ) {
            onWitherArmorEffectApplied()
            previousWitherArmorState = true
        }
        previousWitherArmorDuration = witherArmorStatusEffect?.duration?.toLong() ?: 0L

        if (edgeThresholdAnimation.animatedValue >= 1.0) {
            PostProcessRenderer.postProcessShaders.remove(edgeHighlightShader)
        }

        /**
        for (particleSystem in particleSystems) {
        val particleState = (particleSystem.particleSpawnModules.first { it is InitializeParticleModule } as InitializeParticleModule).particleState
        if (minecraft.player != null) {
        particleState.x = 0F
        particleState.y = 0F
        particleState.z = 0F

        val multiplier = 4F

        particleState.colorR = 1.0F * multiplier
        particleState.colorG = 0.5F * multiplier
        particleState.colorB = 1.0F * multiplier
        particleState.colorA = 1.0F

        particleState.spriteSize = 8.0F
        particleState.scale = 10F

        particleState.velocityX = 0F
        particleState.velocityY = 0F
        particleState.velocityZ = 0F
        }
        }
         **/
        // val count = 10

        // index += count
        // if (index > particleSystem.particleCount) {
        //     index = 0
        // }
        // println("Spawn: ")
        // particleSystem.particleStates.retrieve().use {
        //     it.particles.forEach { particle ->
        //         println("${particle.x}, ${particle.y}, ${particle.z}, colorRGBA: ${particle.colorR},${particle.colorG},${particle.colorB},${particle.colorA}, velocity: ${particle.velocityX}, ${particle.velocityY}, ${particle.velocityZ}")
        //     }
        // }
        // println("Update: ")
        // particleSystem.updateParticles()
        // particleSystem.particleStates.retrieve().use {
        //     it.particles.forEach { particle ->
        //         println("$particle")
        //     }
        // }
        // println("===index=$index===")

        // }
    }

    fun onWitherArmorEffectApplied() {
        previousWitherArmorState = true
        val witherArmorStatusEffect = player.getStatusEffect(WITHER_ARMOR_EFFECT)
        previousWitherArmorDuration = witherArmorStatusEffect?.duration?.toLong() ?: 0L

        PostProcessRenderer.postProcessShaders.add(colorFilterShader)
        PostProcessRenderer.postProcessShaders.add(edgeHighlightShader)
        PostProcessRenderer.postProcessShaders.add(radialBlurShader)

        ghostStrengthAnimation.from = 5.0
        ghostStrengthAnimation.to = ghostStrengthAnimation.value
        ghostStrengthAnimation.duration = Duration.ofMillis(1000)
        ghostStrengthAnimation.start()

        colorAnimation.red.from = 2.0
        colorAnimation.red.duration = Duration.ofMillis(10000)
        colorAnimation.red.to = if (previousAngryState) {
            2.0
        } else {
            1.0
        }
        colorAnimation.red.start()

        edgeThresholdAnimation.from = 0.0

        edgeThresholdAnimation.to = edgeThresholdAnimation.value
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

        ShockwaveRenderer.wavePosition = player.pos.toVector3f()

        ShockwaveRenderer.waveRadius.from = .0
        ShockwaveRenderer.waveRadius.to = 128.0
        ShockwaveRenderer.waveRadius.duration = Duration.ofMillis(1000)
        ShockwaveRenderer.waveRadius.start()

        ShockwaveRenderer.waveSize.from = 1.0
        ShockwaveRenderer.waveSize.to = .0
        ShockwaveRenderer.waveSize.duration = Duration.ofMillis(1000)
        ShockwaveRenderer.waveSize.start()
    }

    private fun onAngeredEffectApplied() {
        PostProcessRenderer.postProcessShaders.add(colorFilterShader)
        PostProcessRenderer.postProcessShaders.add(edgeHighlightShader)
        PostProcessRenderer.postProcessShaders.add(radialBlurShader)
        // PostProcessRenderer.postProcessShaders.add(ghostShader)

        colorAnimation.red.value = 2.0
        colorAnimation.red.duration = Duration.ofMillis(1000)

        colorAnimation.green.value = 1.0
        colorAnimation.green.duration = Duration.ofMillis(1000)

        colorAnimation.blue.value = 1.0

        edgeThresholdAnimation.value = 0.3
        edgeThresholdAnimation.duration = Duration.ofMillis(1000)
        blurRadiusAnimation.value = 5.0

        ghostStrengthAnimation.duration = Duration.ofMillis(1000)
        ghostStrengthAnimation.value = 1.0

        auraAlphaAnimation.value = 1.0
    }

    private fun onAngeredEffectRemoved() {
        colorAnimation.red.value = 1.0
        colorAnimation.green.value = 1.0
        colorAnimation.blue.value = 1.0
        edgeThresholdAnimation.value = 1.0
        ghostStrengthAnimation.value = 0.0
        auraAlphaAnimation.value = .0
    }

    private val sceneFramebuffer by lazy { PostProcessRenderer.createManagedFramebuffer() }
    private var previousFramebuffer = -1
    private var useAuraShader = false
    private var useBloom = false

    @JvmStatic
    fun beginRenderEntity() {
        if (player.isBloodPactActive) {
            useBloom = false
        } else if (previousAngryState || auraAlphaAnimation.animatedValue != .0) {
            useAuraShader = false // Disabled temporarily
        }

        if (!useBloom && !useAuraShader) {
            return
        }

        previousFramebuffer = GlStateManager.getBoundFramebuffer()
        sceneFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC)
        sceneFramebuffer.beginWrite(false)
    }

    @JvmStatic
    fun endRenderEntity() {
        StateIsolation.isolate(
            FramebufferState.captureSnapshot(), ViewportState.captureSnapshot(),
            BlendState.captureSnapshot(), BlendFuncSeparateState.captureSnapshot()
        ) {
            val sculkCatalystIsAlreadyActive = SculkCatalystMagic.isSculkCatalystActive(player)
            if (!sculkCatalystIsAlreadyActive) {
                CollapseEffectRenderer.dissolveFactor.value = .0
                CollapseEffectRenderer.dissolveFactor.duration = Duration.ofMillis(300)
            }
            if (CollapseEffectRenderer.dissolveFactor.animatedValue != .0) {
                PostProcessRenderer.clearFramebuffers()

                CollapseEffectRenderer.depthAttachment = minecraft.framebuffer.depthAttachment
                PostProcessRenderer.renderShaderToFramebuffer(CollapseEffectRenderer.shader, PostProcessRenderer.ping)
                PostProcessRenderer.copyFramebuffer(PostProcessRenderer.ping, minecraft.framebuffer, false)
            }
            if (ShockwaveRenderer.waveRadius.animatedValue != .0) {
                PostProcessRenderer.clearFramebuffers()

                ShockwaveRenderer.depthAttachment = minecraft.framebuffer.depthAttachment
                PostProcessRenderer.renderShaderToFramebuffer(ShockwaveRenderer.shockwaveShader, PostProcessRenderer.ping)
                PostProcessRenderer.copyFramebuffer(PostProcessRenderer.ping, minecraft.framebuffer, false)
            }
        }

        particleSystem.updateParticles()
        ExplosionParticle.particleSystem.updateParticles()
        StateIsolation.isolate(DepthTestState(true), BlendState(false)) {
            particleSystem.renderParticles()
            ExplosionParticle.particleSystem.renderParticles()
        }

        SculkCatalystEffectRenderer.render()

        if (!useBloom) {
            return
        }

        BloomEffect.brightnessThreshold = -1F
        BloomEffect.brightnessPassFramebuffer = sceneFramebuffer
        BloomEffect.renderBloom()

        StateIsolation.isolate(
            FramebufferState.captureSnapshot(), ViewportState.captureSnapshot(),
            BlendState(true), BlendFuncState(GL_ONE, GL_ONE)
        ) {
            PostProcessRenderer.copyFramebuffer(BloomEffect.bloomUpFramebuffer, minecraft.framebuffer, false)
        }

        StateIsolation.isolate(
            FramebufferState.captureSnapshot(), ViewportState.captureSnapshot(),
            BlendState(true), BlendFuncSeparateState(),
        ) {
            PostProcessRenderer.copyFramebuffer(sceneFramebuffer, minecraft.framebuffer, false)
        }
        useBloom = false
    }
}
