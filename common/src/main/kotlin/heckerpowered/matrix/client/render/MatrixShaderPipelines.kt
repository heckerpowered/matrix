/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.pipeline.BindGroupLayout
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.core.resource.ClasspathResourceProvider
import heckerpowered.matrix.core.resourceToString
import net.minecraft.client.renderer.BindGroupLayouts
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier

/**
 * Vulkan/OpenGL compatible shader pipeline bridge for Matrix post effects.
 *
 * Minecraft 26.2 compiles shaders through RenderPipeline/GpuDevice instead of
 * exposing backend-specific OpenGL program ids. This object only owns pipeline
 * descriptions and precompilation; actual post-process drawing must submit these
 * pipelines through a RenderPass.
 */
object MatrixShaderPipelines {
    data class PostProcessPass(
        val fragmentShader: String,
        val pipeline: RenderPipeline,
        val samplers: List<String>,
        val uniformBlocks: List<String>,
    ) {
        fun supportsSingleInput(): Boolean {
            return samplers.isEmpty() || samplers.all(SINGLE_INPUT_SAMPLERS::contains)
        }
    }

    private val postProcessPipelines = linkedMapOf<String, PostProcessPass>()
    private val compiledPipelines = linkedMapOf<String, CompiledRenderPipeline>()
    private var pointSpritePipeline: RenderPipeline? = null

    private val fallbackPostFragments = listOf(
        "post/dissolve/dissolve",
        "post/dissolve/texture_dissolve",
        "post/aura",
        "post/blend_screen",
        "post/blur/gaussian_blur",
        "post/blur/kawase_blur",
        "post/blur/radial_blur",
        "post/blur/tent",
        "post/bloom/bloom_brightness_pass",
        "post/bloom/bloom_lower_sampling_pass",
        "post/bloom/bloom_super_sampling_pass",
        "post/circle",
        "post/collapse",
        "post/color/colorful",
        "post/color_filter",
        "post/color_fusion",
        "post/edge_highlight",
        "post/ghost",
        "post/grain/background_grain",
        "post/hud/progress_ring",
        "post/lower_sampling/hybrid_lod",
        "post/lower_sampling/lod",
        "post/opacity_blend",
        "post/opacity_mask",
        "post/refraction/refraction",
        "post/sampling/bilinear",
        "post/sdf/drop_shadow",
        "post/sdf/jump_flooding",
        "post/sdf/sdf_eval",
        "post/sdf/seed_gen",
        "post/shockwave",
        "post/the_world",
        "post/tone_mapping/aces_filmic",
        "post/velocity_map/velocity_map",
        "post/the_world",
        "post/vortex/vortex",
        "post/vortex/inverse_vortex",
    )

    fun postProcessPipeline(fragmentShader: String): RenderPipeline {
        return postProcessPass(fragmentShader).pipeline
    }

    fun postProcessPass(fragmentShader: String): PostProcessPass {
        val normalizedFragment = normalizeShaderId(fragmentShader)
        return postProcessPipelines.getOrPut(normalizedFragment) {
            val samplers = parseSamplerNames(normalizedFragment)
            val uniformBlocks = parseUniformBlockNames(normalizedFragment)
            val builder = RenderPipeline.builder()
                    .withLocation(Matrix.identifier("pipeline/${normalizedFragment.replace('/', '_')}"))
                    .withVertexShader(Matrix.identifier("core/screenquad_frag_tex_coord"))
                    .withFragmentShader(Matrix.identifier(normalizedFragment))
                    .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
            if (samplers.isNotEmpty()) {
                builder.withBindGroupLayout(bindGroupLayout(samplers, uniformBlocks))
            } else if (uniformBlocks.isNotEmpty()) {
                builder.withBindGroupLayout(bindGroupLayout(samplers, uniformBlocks))
            }

            PostProcessPass(normalizedFragment, RenderPipelines.register(builder.build()), samplers, uniformBlocks)
        }
    }

    fun precompileKnownPostPipelines() {
        val postFragments = discoverPostFragments()
        val compiledPostPipelines = postFragments.count(::precompilePostPipeline)
        val compiledPointSprite = precompilePointSpritePipeline()
        Matrix.LOGGER.info(
            "Matrix shader pipelines precompiled: {}/{} post effects, point sprite={}",
            compiledPostPipelines,
            postFragments.size,
            compiledPointSprite,
        )
    }

    fun precompilePostPipeline(fragmentShader: String): Boolean {
        val normalizedFragment = normalizeShaderId(fragmentShader)
        val pipeline = postProcessPipeline(normalizedFragment)
        val device = RenderSystem.tryGetDevice() ?: return false
        val result = runCatching {
            val compiled = device.precompilePipeline(pipeline)
            compiledPipelines[normalizedFragment] = compiled
            compiled.isValid
        }
        result.exceptionOrNull()?.let {
            Matrix.LOGGER.warn("Matrix post shader pipeline failed to precompile: {}", normalizedFragment, it)
        }
        val valid = result.getOrDefault(false)
        if (!valid && result.exceptionOrNull() == null) {
            Matrix.LOGGER.warn("Matrix post shader pipeline is invalid after precompile: {}", normalizedFragment)
        }
        return valid
    }

    fun isPostPipelineCompiled(fragmentShader: String): Boolean {
        return compiledPipelines[normalizeShaderId(fragmentShader)]?.isValid == true
    }

    fun isSingleInputPostPipelineCompiled(fragmentShader: String): Boolean {
        val normalizedFragment = normalizeShaderId(fragmentShader)
        return postProcessPass(normalizedFragment).supportsSingleInput() && isPostPipelineCompiled(normalizedFragment)
    }

    fun singleInputSamplerNames(fragmentShader: String): List<String> {
        return postProcessPass(fragmentShader).samplers.filter(SINGLE_INPUT_SAMPLERS::contains)
    }

    fun pointSpritePipeline(): RenderPipeline {
        return pointSpritePipeline ?: RenderPipelines.register(
            RenderPipeline.builder()
                .withLocation(Matrix.identifier("pipeline/point_sprite"))
                .withVertexShader(Matrix.identifier("point_sprite/point_sprite"))
                .withFragmentShader(Matrix.identifier("point_sprite/point_sprite"))
                .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.POINTS)
                .withCull(false)
                .withColorTargetState(ColorTargetState(BlendFunction.ADDITIVE))
                .build()
        ).also {
            pointSpritePipeline = it
        }
    }

    fun precompilePointSpritePipeline(): Boolean {
        val pipeline = pointSpritePipeline()
        val device = RenderSystem.tryGetDevice() ?: return false
        val result = runCatching {
            val compiled = device.precompilePipeline(pipeline)
            compiledPipelines["point_sprite/point_sprite"] = compiled
            compiled.isValid
        }
        result.exceptionOrNull()?.let {
            Matrix.LOGGER.warn("Matrix point sprite pipeline failed to precompile", it)
        }
        val valid = result.getOrDefault(false)
        if (!valid && result.exceptionOrNull() == null) {
            Matrix.LOGGER.warn("Matrix point sprite pipeline is invalid after precompile")
        }
        return valid
    }

    private fun normalizeShaderId(path: String): String {
        return path
            .removePrefix("/")
            .removePrefix("assets/matrix/shaders/")
            .removeSuffix(".fsh")
            .removeSuffix(".frag")
            .removeSuffix(".vsh")
    }

    private fun discoverPostFragments(): List<String> {
        val discovered = runCatching {
            ClasspathResourceProvider.Default
                .listRecursively("assets/matrix/shaders/post")
                .asSequence()
                .filter { it.endsWith(".fsh") || it.endsWith(".frag") }
                .map(::normalizeShaderId)
                .toList()
        }.getOrDefault(emptyList())
        return (fallbackPostFragments + discovered).distinct()
    }

    private fun parseSamplerNames(fragmentShader: String): List<String> {
        val source = shaderSource(fragmentShader) ?: return emptyList()
        return SAMPLER_REGEX.findAll(source)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }

    private fun parseUniformBlockNames(fragmentShader: String): List<String> {
        val source = shaderSource(fragmentShader) ?: return emptyList()
        return UNIFORM_BLOCK_REGEX.findAll(source)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }

    private fun shaderSource(fragmentShader: String): String? {
        val normalized = normalizeShaderId(fragmentShader)
        return runCatching { resourceToString("/assets/matrix/shaders/$normalized.fsh") }
            .recoverCatching { resourceToString("/assets/matrix/shaders/$normalized.frag") }
            .getOrNull()
    }

    private fun bindGroupLayout(samplers: List<String>, uniformBlocks: List<String>): BindGroupLayout {
        val builder = BindGroupLayout.builder()
        for (sampler in samplers) {
            builder.withSampler(sampler)
        }
        for (uniformBlock in uniformBlocks) {
            builder.withUniform(uniformBlock, com.mojang.blaze3d.shaders.UniformType.UNIFORM_BUFFER)
        }
        return builder.build()
    }

    private val SAMPLER_REGEX = Regex("""uniform\s+sampler\w+\s+([A-Za-z_][A-Za-z0-9_]*)""")
    private val UNIFORM_BLOCK_REGEX = Regex("""layout\s*\(\s*std140\s*\)\s*uniform\s+([A-Za-z_][A-Za-z0-9_]*)""")

    private val SINGLE_INPUT_SAMPLERS = setOf(
        "InSampler",
        "framebuffer",
        "colorAttachment",
        "hdrScene",
    )
}
