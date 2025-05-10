package heckerpowered.matrix.client.render

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.event.PostProcessCallback
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.render.particle.ParticleSystem
import heckerpowered.matrix.client.render.particle.module.particle_render.ParticleSpriteRendererModule
import heckerpowered.matrix.client.render.particle.module.particle_spawn.InitializeParticleModule
import heckerpowered.matrix.client.render.post.BloomEffect
import heckerpowered.matrix.client.render.shader.RadialBlurRenderer.samples
import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.DissolveShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.client.ui.foundation.animation.ColorAnimation
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.effect.MatrixStatusEffects.ANGERED_EFFECT
import heckerpowered.matrix.common.effect.MatrixStatusEffects.WITHER_ARMOR_EFFECT
import heckerpowered.matrix.common.effect.bloodPactActive
import heckerpowered.matrix.core.approximatelyEqual
import heckerpowered.matrix.core.resourceToString
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL31
import org.lwjgl.opengl.GL46.glUniform1f
import org.lwjgl.opengl.GL46.glUniform1i
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
        BlitShader(
            resourceToString("/assets/matrix/shaders/sobel.vert"),
            resourceToString("/assets/matrix/shaders/post/aura.fsh"),
            arrayOf(
                UniformProvider("entityDepthAttachment") { pointer ->
                    GL31.glActiveTexture(GlConst.GL_TEXTURE0)
                    GL31.glBindTexture(GlConst.GL_TEXTURE_2D, sceneFramebuffer.depthAttachment)
                    RenderSystem.glUniform1i(pointer, 0)
                },
                UniformProvider("entityColorAttachment") { pointer ->
                    GL31.glActiveTexture(GlConst.GL_TEXTURE0 + 1)
                    GL31.glBindTexture(GlConst.GL_TEXTURE_2D, sceneFramebuffer.colorAttachment)
                    RenderSystem.glUniform1i(pointer, 1)
                },
                UniformProvider("sceneDepthAttachment") { pointer ->
                    GL31.glActiveTexture(GlConst.GL_TEXTURE0 + 2)
                    GL31.glBindTexture(GlConst.GL_TEXTURE_2D, minecraft.framebuffer.depthAttachment)
                    RenderSystem.glUniform1i(pointer, 2)
                },
                UniformProvider("sceneColorAttachment") { pointer ->
                    GL31.glActiveTexture(GlConst.GL_TEXTURE0 + 3)
                    GL31.glBindTexture(GlConst.GL_TEXTURE_2D, minecraft.framebuffer.colorAttachment)
                    RenderSystem.glUniform1i(pointer, 3)
                },
                UniformProvider("noiseColorAttachment") { pointer ->
                    GL31.glActiveTexture(GlConst.GL_TEXTURE0 + 4)
                    GL31.glBindTexture(GlConst.GL_TEXTURE_2D, DissolveShader.perlinNoiseTextureId)
                    RenderSystem.glUniform1i(pointer, 4)
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
                    GL31.glUniform4f(
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
    private val bloomThresholdAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))

    private val colorFilterShader by lazy {
        BlitShader(
            resourceToString("/assets/matrix/shaders/sobel.vert"),
            resourceToString("/assets/matrix/shaders/post/color_filter.fsh"),
            arrayOf(
                PostProcessRenderer.framebufferProvider,
                UniformProvider("color") { pointer ->
                    GL20.glUniform4f(
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
        BlitShader(
            resourceToString("/assets/matrix/shaders/sobel.vert"),
            resourceToString("/assets/matrix/shaders/post/edge_highlight.fsh"),
            arrayOf(
                PostProcessRenderer.framebufferProvider,
                UniformProvider("edgeThreshold") { pointer ->
                    glUniform1f(pointer, edgeThresholdAnimation.animatedValue.toFloat())
                },
                UniformProvider("edgeColor") { pointer ->
                    GL20.glUniform4f(pointer, 0.7F, 0.1F, 0.1F, 1.0F)
                }
            )
        )
    }

    private val radialBlurShader by lazy {
        BlitShader(
            resourceToString("/assets/matrix/shaders/sobel.vert"),
            resourceToString("/assets/matrix/shaders/post/blur/radial_blur.fsh"),
            arrayOf(
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
        PostProcessCallback.event.register(::onPostProcess)

        colorAnimation.red.start()
        colorAnimation.green.start()
        colorAnimation.blue.start()

        edgeThresholdAnimation.start()
        edgeThresholdAnimation.animatedValue = 1.0
        bloomThresholdAnimation.animatedValue = 1.0
    }

    private fun onPostProcess() {
        if (bloomThresholdAnimation.animatedValue.approximatelyEqual(1.0)) {
            // return
        }

        spoofFramebuffer {
            minecraft.framebuffer.beginWrite(false)
            particleSystem.updateParticles()
            particleSystem.renderParticles()

            BloomEffect.brightnessThreshold = 1.0F// bloomThresholdAnimation.animatedValue.toFloat()
            BloomEffect.brightnessPassFramebuffer = minecraft.framebuffer
            BloomEffect.renderBloom()

            RenderSystem.enableBlend()
            RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE)
            PostProcessRenderer.copyFramebuffer(BloomEffect.bloomFramebuffer, minecraft.framebuffer, false)
            RenderSystem.defaultBlendFunc()
        }
    }

    private val particleSystem by lazy {
        ParticleSystem(
            100,
            particleSpawnModules = arrayOf(
                InitializeParticleModule()
            ),
            particleUpdateModules = arrayOf(
                // ParticleStateModule(),
                // AddVelocityModule(Vector3f(0F, 1.0F, 0F))
            ),
            particleRenderModules = arrayOf(
                ParticleSpriteRendererModule()
            )
        )
    }

    // private var index: Int = 0

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

        // if (index >= particleSystem.particleStates.particleCount) {
        //     index = 0
        // }
        // val particleState = (particleSystem.particleSpawnModules[0] as InitializeParticleModule).particleState
        // if (minecraft.player != null) {
        //     particleState.x = 0F
        //     particleState.y = 0F
        //     particleState.z = 0F
//
        //     particleState.colorR = 1.0F
        //     particleState.colorG = 1.0F
        //     particleState.colorB = 1000.0F
        //     particleState.colorA = 1.0F
//
        //     particleState.spriteSize = 8.0F
        //     particleState.scale = 10F
        // }
        // particleSystem.spawnPartialParticles(index, 2)
        // index += 1
        // if (index % 20 == 0) {
        //     particleSystem.particleStates.initParticles {
        //         it.x = 0F
        //         it.y = Random.nextFloat() * 10
        //         it.z = 0F
//
        //         it.colorR = 1.0F
        //         it.colorG = 1.0F
        //         it.colorB = 1000.0F
        //         it.colorA = 1.0F
//
        //         it.spriteSize = 8.0F
        //         it.scale = 10F
        //     }
        //     println("===index=$index===")
        //     particleSystem.particleStates.retrieve().use {
        //         it.particles.forEach { particle ->
        //             println("${particle.x}, ${particle.y}, ${particle.z}")
        //         }
        //     }
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

        bloomThresholdAnimation.from = .0
        bloomThresholdAnimation.to = 1.0
        bloomThresholdAnimation.duration = Duration.ofSeconds(1)
        bloomThresholdAnimation.start()
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
        if (player.bloodPactActive) {
            useBloom = true
        } else if (previousAngryState || auraAlphaAnimation.animatedValue != .0) {
            useAuraShader = true
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
        if (!useAuraShader && !useBloom) {
            return
        }
        // RenderSystem.enableBlend()
        // PostProcessRenderer.copyFramebuffer(sceneFramebuffer, sceneFramebuffer)

        // BloomEffect.brightnessThreshold = 0F
        // BloomEffect.brightnessPassFramebuffer = sceneFramebuffer
        // BloomEffect.renderBloom()
//
        // RenderSystem.enableBlend()
        // PostProcessRenderer.copyFramebuffer(BloomEffect.bloomFramebuffer, minecraft.framebuffer, false)

        // copyFramebuffer() will discard all full black pixels.
        spoofFramebuffer {
            if (useBloom) {
                BloomEffect.brightnessThreshold = .0F
                BloomEffect.brightnessPassFramebuffer = sceneFramebuffer
                BloomEffect.renderBloom()
                minecraft.framebuffer.beginWrite(true)
                BloomEffect.bloomFramebuffer.apply {
                    RenderSystem.enableDepthTest()
                    RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE)
                    draw(viewportWidth, viewportHeight, false)
                    RenderSystem.defaultBlendFunc()
                    sceneFramebuffer.draw(viewportWidth, viewportHeight, false)
                }
            }
            if (useAuraShader) {
                PostProcessRenderer.useDepthAttachment = true
                minecraft.framebuffer.beginWrite(true)
                RenderSystem.enableBlend()
                RenderSystem.enableDepthTest()
                auraShader.blit()
            }
            PostProcessRenderer.useDepthAttachment = false
        }
        useAuraShader = false
        useBloom = false
        minecraft.framebuffer.beginWrite(true)
    }
}