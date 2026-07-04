/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.event.PostProcessCallback
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.render.effect.SculkCatalystEffectRenderer
// GPU particle system retired (see common/attic)
// import heckerpowered.matrix.client.render.particle.ParticleSystem
// import heckerpowered.matrix.client.render.particle.memory.MemoryLayout
// import heckerpowered.matrix.client.render.particle.module.particle_render.ParticleSpriteRendererModule
// import heckerpowered.matrix.client.render.particle.module.particle_spawn.InitializeParticleModule
// import heckerpowered.matrix.client.render.particle.module.particle_spawn.RandomLifetimeModule
// import heckerpowered.matrix.client.render.particle.module.particle_spawn.RandomVelocityModule
// import heckerpowered.matrix.client.render.particle.module.particle_update.DragModule
// import heckerpowered.matrix.client.render.particle.module.particle_update.KillParticleModule
// import heckerpowered.matrix.client.render.particle.module.particle_update.ParticleStateModule
// import heckerpowered.matrix.client.render.particle.module.particle_update.ScaleSpriteSizeBySpeedModule
// import heckerpowered.matrix.client.render.particle.system.ExplosionParticle
import heckerpowered.matrix.client.render.post.BloomEffect
import heckerpowered.matrix.client.render.post.CollapseEffectRenderer
import heckerpowered.matrix.client.render.post.ShockwaveRenderer
import heckerpowered.matrix.client.render.shader.RadialBlurRenderer.samples
import heckerpowered.matrix.client.render.shader.VortexRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.DissolveShader
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.client.ui.foundation.animation.ColorAnimation
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.effect.ModMobEffects.Angered
import heckerpowered.matrix.common.effect.ModMobEffects.WitherArmor
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.magic.spell.SculkCatalystMagic
import heckerpowered.matrix.core.approximatelyEqual
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
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
            "post/aura.fsh",
            uniforms = arrayOf(
                UniformProvider("MatrixPostUniforms") {
                    // auraParams: x = time, y = alpha
                    val age = minecraft.player?.tickCount?.toFloat() ?: 0F
                    val deltaTime = minecraft.deltaTracker.getGameTimeDeltaPartialTick(true)
                    val time = age + deltaTime
                    putVec4(time / 1000.0F, auraAlphaAnimation.animatedValue.toFloat(), 0F, 0F)
                    // auraColor: preserves original behavior of using color.red for all 4 components
                    val color = colorAnimation
                    putVec4(
                        color.red.animatedValue.toFloat() / color.red.to.toFloat(),
                        color.red.animatedValue.toFloat() / color.red.to.toFloat(),
                        color.red.animatedValue.toFloat() / color.red.to.toFloat(),
                        color.red.animatedValue.toFloat() / color.red.to.toFloat()
                    )
                }
            ),
            textures = arrayOf(
                TextureProvider("entityDepthAttachment") { sceneFramebuffer.depthTextureView },
                TextureProvider("entityColorAttachment") { sceneFramebuffer.colorTextureView },
                TextureProvider("sceneDepthAttachment") { minecraft.mainRenderTarget.depthTextureView },
                TextureProvider("sceneColorAttachment") { minecraft.mainRenderTarget.colorTextureView },
                TextureProvider("noiseColorAttachment") { DissolveShader.perlinNoiseTextureView }
            )
        )
    }

    private val blurRadiusAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))

    private val edgeThresholdAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))
    val bloomThresholdAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))

    private val colorFilterShader by lazy {
        BlitProgram(
            "post/color_filter.fsh",
            uniforms = arrayOf(
                UniformProvider("MatrixPostUniforms") {
                    // MatrixPostData0 = color
                    putVec4(
                        colorAnimation.red.animatedValue.toFloat(),
                        colorAnimation.green.animatedValue.toFloat(),
                        colorAnimation.blue.animatedValue.toFloat(),
                        1.0F
                    )
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                }
            ),
            textures = arrayOf(PostProcessRenderer.framebufferProvider)
        )
    }

    private val edgeHighlightShader by lazy {
        BlitProgram(
            "post/edge_highlight.fsh",
            uniforms = arrayOf(
                UniformProvider("MatrixPostUniforms") {
                    // MatrixPostData0.x = edgeThreshold
                    putVec4(edgeThresholdAnimation.animatedValue.toFloat(), 0F, 0F, 0F)
                    // MatrixPostData1 = edgeColor
                    putVec4(0.7F, 0.1F, 0.1F, 1.0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                }
            ),
            textures = arrayOf(PostProcessRenderer.framebufferProvider)
        )
    }

    private val radialBlurShader by lazy {
        BlitProgram(
            "post/blur/radial_blur.fsh",
            uniforms = arrayOf(
                UniformProvider("MatrixPostUniforms") {
                    // MatrixPostData0.x = strength, .y = samples
                    putVec4(ghostStrengthAnimation.animatedValue.toFloat(), samples.toFloat(), 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                }
            ),
            textures = arrayOf(PostProcessRenderer.framebufferProvider)
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
        VortexRenderer.render()

        bloomThresholdAnimation.animatedValue = 1.0
        if (!shouldRenderBloom()) {
            return
        }

        BloomEffect.brightnessThreshold = bloomThresholdAnimation.animatedValue.toFloat() + 0.1F
        BloomEffect.brightnessPassFramebuffer = minecraft.mainRenderTarget
        BloomEffect.renderBloom()

        PostProcessRenderer.copyFramebuffer(BloomEffect.bloomUpFramebuffer, minecraft.mainRenderTarget, BlendFunction.ADDITIVE)

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

    // GPU particle system retired (see common/attic)
    // val particleSystem by lazy {
    //     ParticleSystem(
    //         10000,
    //         particleSpawnModules = arrayOf(
    //             InitializeParticleModule(),
    //             RandomVelocityModule(),
    //             RandomLifetimeModule(),
    //         ),
    //         particleUpdateModules = arrayOf(
    //             KillParticleModule(),
    //             ParticleStateModule(),
    //             DragModule(),
    //             ScaleSpriteSizeBySpeedModule()
    //         ),
    //         particleRenderModules = arrayOf(
    //             ParticleSpriteRendererModule()
    //         ),
    //         MemoryLayout.DEFAULT_LAYOUT
    //     )
    // }

    @Suppress("UNUSED_PARAMETER")
    fun spawnParticleAt(position: Vec3, count: Int = 1) {
        // GPU particle system retired (see common/attic)
        // val particleState = (particleSystem.particleSpawnModules.first { it is InitializeParticleModule } as InitializeParticleModule).particleState
        // particleState.x = position.x.toFloat()
        // particleState.y = position.y.toFloat()
        // particleState.z = position.z.toFloat()
        //
        // val multiplier = 4F
        //
        // particleState.colorR = 1.0F * multiplier
        // particleState.colorG = 0.5F * multiplier
        // particleState.colorB = 1.0F * multiplier
        // particleState.colorA = 1.0F
        //
        // particleState.spriteSize = 80.0F
        // particleState.scale = 1F
        //
        // index += count
        // if (index > particleSystem.particleCount) {
        //     index = 0
        // }
        // particleSystem.spawnPartialParticles(index, count)
    }

    var index: Int = 0

    fun onTick(minecraftClient: Minecraft) {
        if (minecraftClient.player == null) {
            return
        }
        if (player.getEffect(Angered) == null && previousAngryState) {
            onAngeredEffectRemoved()
            previousAngryState = false
        } else if (player.getEffect(Angered) != null && !previousAngryState) {
            onAngeredEffectApplied()
            previousAngryState = true
        }

        val witherArmorStatusEffect = player.getEffect(WitherArmor)
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
        val witherArmorStatusEffect = player.getEffect(WitherArmor)
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

        ShockwaveRenderer.wavePosition = player.position().toVector3f()

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

        PostProcessRenderer.clear(sceneFramebuffer)
        RenderSystem.outputColorTextureOverride = sceneFramebuffer.colorTextureView
        RenderSystem.outputDepthTextureOverride = sceneFramebuffer.depthTextureView
    }

    @JvmStatic
    fun endRenderEntity() {
        // Closes the capture redirect opened in beginRenderEntity; the old code implicitly
        // ended capture once the caller resumed writing to the main framebuffer, but under
        // the wrapper API the override must be cleared explicitly.
        RenderSystem.outputColorTextureOverride = null
        RenderSystem.outputDepthTextureOverride = null

        run {
            val sculkCatalystIsAlreadyActive = SculkCatalystMagic.isSculkCatalystActive(player)
            if (!sculkCatalystIsAlreadyActive && CollapseEffectRenderer.dissolveFactor.to != .0) {
                CollapseEffectRenderer.dissolveFactor.value = .0
                CollapseEffectRenderer.dissolveFactor.duration = Duration.ofMillis(300)
            }
            if (CollapseEffectRenderer.dissolveFactor.animatedValue != .0) {
                PostProcessRenderer.clearFramebuffers()

                CollapseEffectRenderer.depthAttachment = minecraft.mainRenderTarget.depthTextureView
                PostProcessRenderer.renderShaderToFramebuffer(CollapseEffectRenderer.shader, PostProcessRenderer.ping)
                // Uncertain: old disableBlend=false with only a captured (snapshot) blend-func
                // state active; no explicit blend func was set right before this call, so
                // blending is treated as disabled here. Revisit if this looks wrong in-game.
                PostProcessRenderer.copyFramebuffer(PostProcessRenderer.ping, minecraft.mainRenderTarget, null)
            }
            if (ShockwaveRenderer.waveRadius.animatedValue != .0) {
                PostProcessRenderer.clearFramebuffers()

                ShockwaveRenderer.depthAttachment = minecraft.mainRenderTarget.depthTextureView
                PostProcessRenderer.renderShaderToFramebuffer(ShockwaveRenderer.shockwaveShader, PostProcessRenderer.ping)
                // Uncertain: see note above for the CollapseEffectRenderer copy.
                PostProcessRenderer.copyFramebuffer(PostProcessRenderer.ping, minecraft.mainRenderTarget, null)
            }
        }

        // GPU particle system retired (see common/attic)
        // particleSystem.updateParticles()
        // ExplosionParticle.particleSystem.updateParticles()
        // StateIsolation.isolate(DepthTestState(true), BlendState(false)) {
        //     particleSystem.renderParticles()
        //     ExplosionParticle.particleSystem.renderParticles()
        // }

        SculkCatalystEffectRenderer.render()

        if (!useBloom) {
            return
        }

        BloomEffect.brightnessThreshold = -1F
        BloomEffect.brightnessPassFramebuffer = sceneFramebuffer
        BloomEffect.renderBloom()

        PostProcessRenderer.copyFramebuffer(BloomEffect.bloomUpFramebuffer, minecraft.mainRenderTarget, BlendFunction.ADDITIVE)

        // Uncertain: old wrapper was a no-arg BlendFuncSeparateState() (default GL_ONE/GL_ZERO
        // reset => effectively opaque copy), so blending is treated as disabled here.
        PostProcessRenderer.copyFramebuffer(sceneFramebuffer, minecraft.mainRenderTarget, null)
        useBloom = false
    }
}
