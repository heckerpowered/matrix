/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.pipeline.BindGroupLayout
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.platform.CompareOp
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.PrimitiveTopology
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.core.resourceToString
import net.minecraft.resources.Identifier
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.OptionalDouble

/**
 * A fullscreen post-process program backed by the 26.2 [RenderPipeline]/[RenderPass] wrapper API,
 * working on both the Vulkan and OpenGL backends.
 *
 * Replaces the former GL-program based implementation: shaders are referenced by resource
 * [Identifier] (loaded and cross-compiled by the vanilla shader pipeline), uniforms live in
 * std140 uniform blocks written through [UniformProvider]s, and textures are bound by sampler
 * name through [TextureProvider]s. Drawing submits a fullscreen triangle (gl_VertexID based,
 * see core/screenquad_frag_tex_coord.vsh) into an explicit [RenderTarget].
 */
open class BlitProgram(
    val fragmentShader: Identifier,
    val vertexShader: Identifier = SCREENQUAD_VERTEX_SHADER,
    val uniforms: Array<UniformProvider> = emptyArray(),
    val textures: Array<TextureProvider> = emptyArray(),
    /** Enables depth writes for shaders that output gl_FragDepth (e.g. blit with depth copy). */
    val writesDepth: Boolean = false,
) {
    constructor(
        fragmentShaderPath: String,
        uniforms: Array<UniformProvider> = emptyArray(),
        textures: Array<TextureProvider> = emptyArray(),
        writesDepth: Boolean = false,
    ) : this(Matrix.identifier(normalizeShaderPath(fragmentShaderPath)), SCREENQUAD_VERTEX_SHADER, uniforms, textures, writesDepth)

    /** Sampler names parsed from the fragment shader source. */
    val samplerNames: List<String> by lazy { parseSamplerNames(fragmentShader) }

    /** std140 uniform block names parsed from the fragment shader source. */
    val uniformBlockNames: List<String> by lazy { parseUniformBlockNames(fragmentShader) }

    private val pipelines = mutableMapOf<Pair<BlendFunction?, GpuFormat>, RenderPipeline>()
    private val uniformBuffers = mutableMapOf<String, UniformRing>()

    fun pipeline(blend: BlendFunction? = null, format: GpuFormat = GpuFormat.RGBA8_UNORM): RenderPipeline {
        return pipelines.getOrPut(blend to format) {
            val builder = RenderPipeline.builder()
                .withLocation(
                    Matrix.identifier(
                        "pipeline/" + fragmentShader.path.replace('/', '_')
                                + (blend?.let { "_blend" } ?: "") + "_" + format.name.lowercase()
                    )
                )
                .withVertexShader(vertexShader)
                .withFragmentShader(fragmentShader)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withCull(false)
                .withDepthStencilState(
                    if (writesDepth) java.util.Optional.of(DepthStencilState(CompareOp.ALWAYS_PASS, true))
                    else java.util.Optional.empty()
                )
            // 26.2 release validates the render pass attachment format against the
            // pipeline's declared color target format, so declare the actual target format.
            builder.withColorTargetState(ColorTargetState(java.util.Optional.ofNullable(blend), format, ColorTargetState.WRITE_ALL))
            if (samplerNames.isNotEmpty() || uniformBlockNames.isNotEmpty()) {
                val layout = BindGroupLayout.builder()
                for (sampler in samplerNames) {
                    layout.withSampler(sampler)
                }
                for (block in uniformBlockNames) {
                    layout.withUniform(block, UniformType.UNIFORM_BUFFER)
                }
                builder.withBindGroupLayout(layout.build())
            }
            builder.build()
        }
    }

    /**
     * Renders this program as a fullscreen pass into [target].
     *
     * @param blend the blend function baked into the pipeline; `null` disables blending,
     *              matching the previous implementation's `blit()` (blend off) default.
     */
    @JvmOverloads
    fun drawTo(target: RenderTarget, blend: BlendFunction? = null) {
        val device = RenderSystem.getDevice()
        val encoder = device.createCommandEncoder()

        val targetFormat = target.colorTexture?.format ?: GpuFormat.RGBA8_UNORM
        val pipeline = pipeline(blend, targetFormat)
        // Mod pipelines are built lazily (after the resource reload that compiles the
        // pipelines registered in RenderPipelines), so the device's default shader source
        // cannot see them; compile explicitly against the ShaderManager-loaded sources.
        // The device caches compiled pipelines, so this is a map lookup on subsequent draws
        // and transparently recompiles after resource reloads clear the pipeline cache.
        device.precompilePipeline(pipeline) { id, type ->
            heckerpowered.matrix.client.minecraft.shaderManager.getShader(id, type)
        }

        // Write uniform blocks before opening the pass: writeToBuffer must not happen inside one.
        val blockSlices = mutableMapOf<String, GpuBufferSlice>()
        for (block in uniformBlockNames) {
            val provider = uniforms.firstOrNull { it.name == block } ?: continue
            val ring = uniformBuffers.getOrPut(block) { UniformRing(fragmentShader, block) }
            blockSlices[block] = ring.write(provider)
        }

        val colorView = target.colorTextureView ?: return
        encoder.createRenderPass(
            { "matrix blit ${fragmentShader.path}" },
            colorView,
            java.util.Optional.empty(),
            if (writesDepth && target.useDepth) target.depthTextureView else null,
            OptionalDouble.empty()
        ).use { pass ->
            pass.setPipeline(pipeline)
            RenderSystem.bindDefaultUniforms(pass)
            for ((block, slice) in blockSlices) {
                pass.setUniform(block, slice)
            }
            for (texture in textures) {
                val view = texture.view() ?: continue
                if (texture.name !in samplerNames) {
                    continue
                }
                val sampler = RenderSystem.getSamplerCache()
                    .getClampToEdge(if (texture.bilinear) FilterMode.LINEAR else FilterMode.NEAREST, texture.mipmap)
                pass.bindTexture(texture.name, view, sampler)
            }
            // draw(vertexCount, instanceCount, firstVertex, firstInstance) — the same
            // fullscreen-triangle call vanilla's PostPass issues (draw(3, 1, 0, 0)); the GL
            // backend maps it to glDrawArraysInstancedBaseInstance(mode, first=arg3,
            // count=arg1, instances=arg2, base=arg4). The argument order matters: with the
            // first two swapped this silently draws zero vertices on both backends.
            pass.draw(3, 1, 0, 0)
        }
    }

    /** Ring of UBO slots so the same program can be drawn several times per frame. */
    private class UniformRing(location: Identifier, block: String) {
        private val slotSize: Long
        private val buffer: GpuBuffer
        private var cursor = 0

        init {
            // Size is bounded by the largest block the mod uses (14 vec4s + matrices);
            // 512 bytes covers two mat4s plus parameters, aligned to the common 256-byte
            // UBO offset alignment requirement.
            slotSize = 512
            buffer = RenderSystem.getDevice().createBuffer(
                { "matrix uniforms ${location.path}#$block" },
                GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_COPY_DST,
                slotSize * SLOTS
            )
        }

        fun write(provider: UniformProvider): GpuBufferSlice {
            val data = ByteBuffer.allocateDirect(slotSize.toInt()).order(ByteOrder.nativeOrder())
            val builder = Std140Builder.intoBuffer(data)
            provider.write(builder)
            val written = builder.get()
            val slice = buffer.slice(slotSize * cursor, slotSize)
            cursor = (cursor + 1) % SLOTS
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(slice, written)
            return slice
        }

        companion object {
            private const val SLOTS = 16
        }
    }

    companion object {
        val SCREENQUAD_VERTEX_SHADER: Identifier = Matrix.identifier("core/screenquad_frag_tex_coord")

        private val SAMPLER_REGEX = Regex("""uniform\s+sampler\w+\s+([A-Za-z_][A-Za-z0-9_]*)""")
        private val UNIFORM_BLOCK_REGEX = Regex("""layout\s*\(\s*std140\s*\)\s*uniform\s+([A-Za-z_][A-Za-z0-9_]*)""")

        fun normalizeShaderPath(path: String): String {
            return path
                .removePrefix("/")
                .removePrefix("assets/matrix/shaders/")
                .removeSuffix(".fsh")
                .removeSuffix(".frag")
                .removeSuffix(".vsh")
                .removeSuffix(".vert")
        }

        private fun shaderSource(identifier: Identifier): String? {
            val base = "/assets/${identifier.namespace}/shaders/${identifier.path}"
            return runCatching { resourceToString("$base.fsh") }
                .recoverCatching { resourceToString("$base.frag") }
                .getOrNull()
        }

        fun parseSamplerNames(identifier: Identifier): List<String> {
            val source = shaderSource(identifier) ?: return emptyList()
            return SAMPLER_REGEX.findAll(source).map { it.groupValues[1] }.distinct().toList()
        }

        fun parseUniformBlockNames(identifier: Identifier): List<String> {
            val source = shaderSource(identifier) ?: return emptyList()
            return UNIFORM_BLOCK_REGEX.findAll(source).map { it.groupValues[1] }.distinct().toList()
        }
    }
}
