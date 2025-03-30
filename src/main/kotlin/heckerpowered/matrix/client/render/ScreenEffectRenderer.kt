package heckerpowered.matrix.client.render

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.client.ui.foundation.animation.ColorAnimation
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.effect.angeredEffect
import heckerpowered.matrix.common.effect.witherArmorEffect
import heckerpowered.matrix.core.resourceToString
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL31
import java.time.Duration

object ScreenEffectRenderer {

    private var previousAngryState = false

    private var previousWitherArmorState = false
    private var previousWitherArmorDuration = 0L

    val colorAnimation = ColorAnimation(
        red = SimpleDoubleAnimation(from = 1.0, to = 1.0, duration = Duration.ofMillis(1000)),
        green = SimpleDoubleAnimation(from = 1.0, to = 1.0, duration = Duration.ofMillis(1000)),
        blue = SimpleDoubleAnimation(from = 1.0, to = 1.0, duration = Duration.ofMillis(1000))
    )
    private val ghostStrengthAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))

    private val auraShader by lazy {
        BlitShader(
            resourceToString("/assets/matrix/shaders/sobel.vert"),
            resourceToString("/assets/matrix/shaders/post/aura.fsh"),
            arrayOf(
                UniformProvider("depthTexture") { pointer ->
                    GL31.glActiveTexture(GlConst.GL_TEXTURE0)
                    GL31.glBindTexture(GlConst.GL_TEXTURE_2D, sceneFramebuffer.depthAttachment)
                    RenderSystem.glUniform1i(pointer, 0)
                },
                UniformProvider("objectDepthTexture") { pointer ->
                    GL31.glActiveTexture(GlConst.GL_TEXTURE1)
                    GL31.glBindTexture(GlConst.GL_TEXTURE_2D, minecraft.framebuffer.depthAttachment)
                    RenderSystem.glUniform1i(pointer, 1)
                },
                UniformProvider("objectTexture") { pointer ->
                    GL31.glActiveTexture(GlConst.GL_TEXTURE2)
                    GL31.glBindTexture(GlConst.GL_TEXTURE_2D, minecraft.framebuffer.colorAttachment)
                    RenderSystem.glUniform1i(pointer, 2)
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

    private val ghostShader by lazy {
        BlitShader(
            resourceToString("/assets/matrix/shaders/sobel.vert"),
            resourceToString("/assets/matrix/shaders/post/ghost.fsh"),
            arrayOf(
                PostProcessRenderer.framebufferProvider,
                UniformProvider("strength") { pointer ->
                    GL20.glUniform1f(
                        pointer,
                        ghostStrengthAnimation.animatedValue.toFloat()
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
                    GL20.glUniform1f(pointer, edgeThresholdAnimation.animatedValue.toFloat())
                },
                UniformProvider("edgeColor") { pointer ->
                    GL20.glUniform4f(pointer, 0.7F, 0.1F, 0.1F, 1.0F)
                }
            )
        )
    }

    fun onInitialize() {
        ClientTickEvents.START_CLIENT_TICK.register(::onTick)

        colorAnimation.red.start()
        colorAnimation.green.start()
        colorAnimation.blue.start()

        edgeThresholdAnimation.start()
        edgeThresholdAnimation.animatedValue = 1.0
    }

    fun onTick(minecraftClient: MinecraftClient) {
        if (minecraftClient.player == null) {
            return
        }
        if (player.getStatusEffect(angeredEffect) == null && previousAngryState) {
            onAngeredEffectRemoved()
            previousAngryState = false
        } else if (player.getStatusEffect(angeredEffect) != null && !previousAngryState) {
            onAngeredEffectApplied()
            previousAngryState = true
        }

        val witherArmorStatusEffect = player.getStatusEffect(witherArmorEffect)
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
    }

    fun onWitherArmorEffectApplied() {
        previousWitherArmorState = true
        val witherArmorStatusEffect = player.getStatusEffect(witherArmorEffect)
        previousWitherArmorDuration = witherArmorStatusEffect?.duration?.toLong() ?: 0L
        
        PostProcessRenderer.postProcessShaders.add(colorFilterShader)
        PostProcessRenderer.postProcessShaders.add(edgeHighlightShader)
        PostProcessRenderer.postProcessShaders.add(ghostShader)

        ghostStrengthAnimation.from = 1.0
        ghostStrengthAnimation.to = ghostStrengthAnimation.value
        ghostStrengthAnimation.duration = Duration.ofMillis(10000)
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
    }

    private fun onAngeredEffectApplied() {
        PostProcessRenderer.postProcessShaders.add(colorFilterShader)
        PostProcessRenderer.postProcessShaders.add(edgeHighlightShader)
        PostProcessRenderer.postProcessShaders.add(ghostShader)

        colorAnimation.red.value = 2.0
        colorAnimation.red.duration = Duration.ofMillis(1000)

        colorAnimation.green.value = 1.0
        colorAnimation.green.duration = Duration.ofMillis(1000)

        colorAnimation.blue.value = 1.0

        edgeThresholdAnimation.value = 0.1
        edgeThresholdAnimation.duration = Duration.ofMillis(1000)
        blurRadiusAnimation.value = 5.0

        ghostStrengthAnimation.duration = Duration.ofMillis(1000)
        ghostStrengthAnimation.value = 1.0
    }

    private fun onAngeredEffectRemoved() {
        colorAnimation.red.value = 1.0
        colorAnimation.green.value = 1.0
        colorAnimation.blue.value = 1.0
        edgeThresholdAnimation.value = 1.0
        ghostStrengthAnimation.value = 0.0
    }

    private val sceneFramebuffer by lazy { PostProcessRenderer.createManagedFramebuffer() }

    @JvmStatic
    fun beginRenderEntity() {
        // val previousFramebuffer = GlStateManager.getBoundFramebuffer()
        // sceneFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC)
        // PostProcessRenderer.copyFramebuffer(minecraft.framebuffer, sceneFramebuffer)
        // GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, previousFramebuffer)
    }

    @JvmStatic
    fun endRenderEntity() {
        // auraShader.blit()
    }
}