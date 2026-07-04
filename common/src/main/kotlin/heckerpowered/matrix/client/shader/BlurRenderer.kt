/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.pipeline.RenderTarget
import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.linearCopyTo
import heckerpowered.matrix.client.render.mainRenderTarget
import heckerpowered.matrix.client.render.nearestCopyTo
import heckerpowered.matrix.client.render.post.ScaleSampling
import heckerpowered.matrix.client.render.shader.GaussianBlurRenderer
import org.joml.Vector2f

object BlurRenderer {
    var initialFramebuffer: RenderTarget = minecraft.mainRenderTarget
    var currentFramebuffer: RenderTarget = minecraft.mainRenderTarget

    var radius = 5.0F
    var kawaseOffset = 1.0F
    var useDownscaling: Boolean = true

    private val halfResolutionBlurFramebuffer by lazy {
        ScaleSampling.createManagedScalingFramebuffer(0.5)
    }

    val blurFramebuffer by lazy {
        PostProcessRenderer.createManagedFramebuffer()
    }

    // 26.2: gaussian_blur_horizontal.fsh / gaussian_blur_vertical.fsh declare
    //   layout(std140) uniform MatrixPostUniforms { vec4 blurParams0; }; #define radius blurParams0.x
    // weight[5] stayed a GLSL-side `const float[]` since it was never varied from Kotlin.
    val horizontalBlurShader = BlitProgram(
        "gaussian_blur_horizontal.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                putVec4(radius * MatrixHud.magicShownOpacityAnimation.animatedValue.toFloat(), 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(PostProcessRenderer.framebufferProvider)
    )

    val verticalBlurShader = BlitProgram(
        "gaussian_blur_vertical.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                putVec4(radius * MatrixHud.magicShownOpacityAnimation.animatedValue.toFloat(), 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(PostProcessRenderer.framebufferProvider)
    )

    // 26.2: post/blur/kawase_blur.fsh declares MatrixPostData0..3; #define offset MatrixPostData0.xy
    val kawaseBlurShader = BlitProgram(
        "post/blur/kawase_blur.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                kawaseOffset += 3F
                val progress = MatrixHud.magicShownOpacityAnimation.animatedValue.toFloat()
                putVec4(progress * kawaseOffset, progress * kawaseOffset, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(PostProcessRenderer.framebufferProvider)
    )

    // 26.2: post/blur/tent.fsh declares MatrixPostData0..3; #define lod MatrixPostData0.x
    val tentBlurShader = BlitProgram(
        "post/blur/tent.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(PostProcessRenderer.framebufferProvider)
    )

    // 26.2: post/color/colorful.fsh declares MatrixPostData0..3;
    //   #define brightness .x / saturation .y / contrast .z (all on MatrixPostData0)
    private val colorfulShader = BlitProgram(
        "post/color/colorful.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                putVec4(1.1F, 2.0F, 1.0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(PostProcessRenderer.framebufferProvider)
    )

    // 26.2: post/blur/blur_mask.fsh has no value uniforms (sampler2D framebuffer only); the old
    // `blurFramebuffer.colorAttachment` binding is now this shader's `framebuffer` TextureProvider.
    val blurTextureRenderProgram = BlitProgram(
        "post/blur/blur_mask.fsh",
        textures = arrayOf(TextureProvider("framebuffer") { blurFramebuffer.colorTextureView })
    )

    fun renderQuad(target: RenderTarget) {
        blurTextureRenderProgram.drawTo(target)
    }

    fun renderGaussianBlurFullResolution(source: RenderTarget = PostProcessRenderer.sourceFramebuffer) {
        PostProcessRenderer.clear(blurFramebuffer)

        GaussianBlurRenderer.direction = Vector2f(1F, 0F)
        GaussianBlurRenderer.colorAttachment = source.colorTextureView
        GaussianBlurRenderer.gaussianBlurShader.drawTo(GaussianBlurRenderer.fullPing)

        GaussianBlurRenderer.direction = Vector2f(0F, 1F)
        GaussianBlurRenderer.colorAttachment = GaussianBlurRenderer.fullPing.colorTextureView
        GaussianBlurRenderer.gaussianBlurShader.drawTo(GaussianBlurRenderer.fullPong)

        GaussianBlurRenderer.fullPong nearestCopyTo blurFramebuffer
    }

    fun renderGaussianBlur(source: RenderTarget = PostProcessRenderer.sourceFramebuffer, target: RenderTarget = blurFramebuffer) {
        source linearCopyTo ScaleSampling.getDownScalingFramebuffer(1.0 / 2)
        ScaleSampling.getDownScalingFramebuffer(1.0 / 2) linearCopyTo ScaleSampling.getDownScalingFramebuffer(1.0 / 4)
        val downscalingFramebuffer = ScaleSampling.getDownScalingFramebuffer(1.0 / 4)

        GaussianBlurRenderer.direction = Vector2f(1F, 0F)
        GaussianBlurRenderer.colorAttachment = downscalingFramebuffer.colorTextureView
        GaussianBlurRenderer.gaussianBlurShader.drawTo(GaussianBlurRenderer.ping)

        GaussianBlurRenderer.direction = Vector2f(0F, 1F)
        GaussianBlurRenderer.colorAttachment = GaussianBlurRenderer.ping.colorTextureView
        GaussianBlurRenderer.gaussianBlurShader.drawTo(GaussianBlurRenderer.pong)

        GaussianBlurRenderer.pong linearCopyTo halfResolutionBlurFramebuffer
        halfResolutionBlurFramebuffer linearCopyTo target
    }

    fun renderKawaseBlur() {
        PostProcessRenderer.sourceFramebuffer linearCopyTo ScaleSampling.getDownScalingFramebuffer(1.0 / 2)
        ScaleSampling.getDownScalingFramebuffer(1.0 / 2) linearCopyTo ScaleSampling.getDownScalingFramebuffer(1.0 / 4)
        val downscalingFramebuffer = ScaleSampling.getDownScalingFramebuffer(1.0 / 4)

        PostProcessRenderer.clear(blurFramebuffer)

        kawaseOffset = 0F
        val shaders = mutableListOf<BlitProgram>()
        for (i in 0..4) {
            shaders.add(kawaseBlurShader)
        }
        PostProcessRenderer.useFramebuffer(downscalingFramebuffer) {
            PostProcessRenderer.renderShadersToFramebuffer(shaders, blurFramebuffer)
        }
    }

    fun renderBlur() {
        renderGaussianBlur()
        // renderKawaseBlur()
    }

    @JvmStatic
    fun onResize(width: Int, height: Int) {
        blurFramebuffer.resize(width, height)
    }
}
