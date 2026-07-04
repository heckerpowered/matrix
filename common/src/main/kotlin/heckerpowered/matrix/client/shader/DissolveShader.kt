/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import org.joml.Vector4f

/**
 * Dissolve-effect state and shaders (mesh-attached "burn away" overlay used by HUD elements).
 *
 * 26.2 note: the previous implementation was a mesh-attached [Program] pair, bound globally via
 * `enableShader()`/`disableShader()` around vanilla [net.minecraft.client.gui.Font]/
 * `BufferBuilder` draw calls -- there is no wrapper-API equivalent to "bind this program
 * globally, then let unrelated immediate-mode code draw against it": render passes are
 * self-contained and target an explicit [com.mojang.blaze3d.pipeline.RenderTarget]. Porting the
 * HUD-mesh draw path itself is out of scope here (owned by whoever converts MatrixHud.kt/
 * ManaBar.kt); this class now only owns the state (uniform values) and the equivalent
 * full-screen [BlitProgram] built from the std140-converted `post/dissolve/dissolve.fsh`, which
 * callers rendering into an explicit target can use directly. [enableShader]/[disableShader]
 * are kept as deprecated no-ops purely so existing call sites keep compiling until they're
 * rewritten against [plainDissolveProgram].
 */
class DissolveShader : AutoCloseable {
    val noiseTexture: GpuTextureView? get() = perlinNoiseTextureView
    var dissolveFactor: Float = 1.0f

    var resolutionX: Float = 1.0F
    var resolutionY: Float = 1.0F
    var emissiveStrength: Float = 15.0F
    var emissiveColor: Vector4f = Vector4f(0.1F, 0.5F, 1.0F, 1.0F)

    // 26.2: post/dissolve/dissolve.fsh declares
    //   layout(std140) uniform MatrixPostUniforms { vec4 dissolveParams0; vec4 dissolveParams1; vec4 dissolveEmissiveColor; };
    //   #define dissolveFactor dissolveParams0.x / emissiveRange .y / emissiveStrength .z / pixelStrength .w
    //   #define detialStrength dissolveParams1.x / time .y / resolution .zw
    // emissiveRange/pixelStrength/detialStrength were never wired to instance state under the old
    // pre-std140 pipeline either (GLSL defaults applied: emissiveRange=0.05, pixelStrength=16.0,
    // detialStrength=1.0). See MatrixPostUniforms.kt@f25647a "post/dissolve/dissolve" for the
    // reference slot layout used here.
    val plainDissolveProgram by lazy {
        BlitProgram(
            "post/dissolve/dissolve.fsh",
            uniforms = arrayOf(
                UniformProvider("MatrixPostUniforms") {
                    putVec4(dissolveFactor, 0.05F, emissiveStrength, 16.0F)
                    putVec4(1.0F, shaderTimeSeconds(), resolutionX, resolutionY)
                    putVec4(emissiveColor)
                }
            ),
            textures = arrayOf(
                TextureProvider("noiseTexture") { noiseTexture }
            )
        )
    }

    companion object {
        private var perlinNoiseTexture: DynamicTexture? = null

        /** Registered under `matrix:textures/noise/perlin_noise.png`; loaded lazily on first use. */
        val perlinNoiseTextureView: GpuTextureView? by lazy { loadPerlinNoiseTexture() }

        private fun loadPerlinNoiseTexture(): GpuTextureView? {
            val identifier = Matrix.identifier("textures/noise/perlin_noise.png")
            val image = minecraft.resourceManager.open(identifier).use { stream -> NativeImage.read(stream) }
            val texture = DynamicTexture({ "matrix perlin noise" }, image)
            texture.upload()
            minecraft.textureManager.register(identifier, texture)
            perlinNoiseTexture = texture

            // TODO(26.2): the old texture set GL_TEXTURE_WRAP_S/T = GL_MIRRORED_REPEAT. The
            // wrapper API's SamplerCache only offers AddressMode.REPEAT / CLAMP_TO_EDGE (no
            // mirrored-repeat mode exists), and BlitProgram's TextureProvider binding path
            // always samples via getClampToEdge(...) regardless -- there is currently no way to
            // reproduce the mirrored-repeat tiling behavior through the core wrapper API.
            return texture.textureView
        }
    }

    @Deprecated("Mesh-attached global shader binding has no 26.2 equivalent; use plainDissolveProgram.drawTo(target) instead")
    fun enableShader() {
    }

    @Deprecated("Mesh-attached global shader binding has no 26.2 equivalent; use plainDissolveProgram.drawTo(target) instead")
    fun disableShader() {
    }

    override fun close() {
    }
}
