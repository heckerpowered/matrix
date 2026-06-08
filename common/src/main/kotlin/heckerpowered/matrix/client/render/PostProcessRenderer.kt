/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.event.PostProcessCallback
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.UniformProvider
import net.minecraft.client.Minecraft
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.renderer.RenderPipelines
import java.util.OptionalDouble
import java.util.OptionalInt

val framebufferProvider: UniformProvider
    get() = PostProcessRenderer.framebufferProvider

object PostProcessRenderer {
    val postProcessShaders = mutableSetOf<BlitProgram>()

    private val minecraftFramebuffer = object : Framebuffer(1, 1, true) {
        override val renderTarget
            get() = Minecraft.getInstance().gameRenderer.mainRenderTarget()
    }

    var sourceFramebuffer: Framebuffer = minecraftFramebuffer
    private var boundFramebuffer: Framebuffer = sourceFramebuffer
    val framebufferProvider = UniformProvider("framebuffer")
    var useDepthAttachment = false
    var levelOfDetail = .0F

    private val managedFramebuffers = mutableListOf<Framebuffer>()
    private val framebuffers = mutableListOf(createFramebuffer(), createFramebuffer())
    private var currentFramebufferIndex = 0

    fun currentFramebuffer(): Framebuffer {
        return framebuffers[currentFramebufferIndex]
    }

    val ping: Framebuffer
        get() = framebuffers[0]

    val pong: Framebuffer
        get() = framebuffers[1]

    fun nextFramebuffer() {
        currentFramebufferIndex++
        if (currentFramebufferIndex >= framebuffers.size) {
            currentFramebufferIndex = 0
        }
    }

    private fun createFramebuffer(): Framebuffer {
        return SimpleFramebuffer(1, 1, true, false)
    }

    fun createManagedFramebuffer(): Framebuffer {
        val framebuffer = SimpleFramebuffer(1, 1, true, false)
        framebuffer.setClearColor(.0F, .0F, .0F, .0F)
        managedFramebuffers.add(framebuffer)
        return framebuffer
    }

    fun manageFramebuffer(framebuffer: Framebuffer) {
        managedFramebuffers.add(framebuffer)
    }

    @JvmStatic
    fun onResize(width: Int, height: Int) {
        minecraftFramebuffer.resize(width, height, false)
        for (framebuffer in framebuffers) {
            framebuffer.resize(width, height, false)
        }
        for (framebuffer in managedFramebuffers) {
            framebuffer.resize(width, height, false)
        }
    }

    @JvmStatic
    fun renderToScreen() {
        if (postProcessShaders.isEmpty()) {
            return
        }

        val renderedFramebuffer = renderPostProcessEffects()
        renderFramebufferToScreen(renderedFramebuffer)
    }

    fun resetFramebuffers() {
        currentFramebufferIndex = 0
        clearFramebuffers()
    }

    fun clearFramebuffers() {
        framebuffers.forEach { it.clear(false) }
    }

    @JvmStatic
    fun renderToFramebuffer(framebuffer: Framebuffer) {
        if (postProcessShaders.isEmpty()) {
            return
        }

        val renderedFramebuffer = renderPostProcessEffects()
        copyFramebuffer(renderedFramebuffer, framebuffer)
    }

    @JvmStatic
    fun renderPostProcessEffects(): Framebuffer {
        return renderShaders(postProcessShaders)
    }

    @JvmStatic
    fun renderToMinecraftFramebuffer() {
        syncMinecraftFramebufferSize()
        if (postProcessShaders.isEmpty()) {
            resetFramebuffers()
        } else {
            renderToFramebuffer(sourceFramebuffer)
        }
        PostProcessCallback.EVENT.invoker().onPostProcess()
    }

    private fun syncMinecraftFramebufferSize() {
        val window = Minecraft.getInstance().window
        if (minecraftFramebuffer.textureWidth != window.width || minecraftFramebuffer.textureHeight != window.height) {
            minecraftFramebuffer.resize(window.width, window.height, false)
        }
    }

    @JvmStatic
    fun renderFramebufferToScreen(framebuffer: Framebuffer, disableBlend: Boolean = false) {
        framebuffer.draw(framebuffer.viewportWidth, framebuffer.viewportHeight, disableBlend)
    }

    @JvmStatic
    fun renderShaderToFramebuffer(shader: BlitProgram, framebuffer: Framebuffer, disableBlend: Boolean = true) {
        renderShader(shader, boundFramebuffer, framebuffer)
        boundFramebuffer = framebuffer
    }

    @JvmStatic
    fun renderShaderToFramebuffer(
        shader: BlitProgram,
        sourceFramebuffer: Framebuffer,
        framebuffer: Framebuffer,
        disableBlend: Boolean = true,
    ) {
        renderShader(shader, sourceFramebuffer, framebuffer)
        boundFramebuffer = framebuffer
    }

    @JvmStatic
    fun renderShaderToFramebuffer(
        shader: BlitProgram,
        framebuffer: Framebuffer,
        textureBindings: Map<String, Framebuffer>,
        disableBlend: Boolean = true,
    ) {
        renderShader(shader, framebuffer, textureBindings)
        boundFramebuffer = framebuffer
    }

    @JvmStatic
    fun renderShaders(shaders: Collection<BlitProgram>): Framebuffer {
        resetFramebuffers()
        var inputFramebuffer = sourceFramebuffer
        for (shader in shaders) {
            val outputFramebuffer = currentFramebuffer()
            renderShader(shader, inputFramebuffer, outputFramebuffer)
            inputFramebuffer = outputFramebuffer
            nextFramebuffer()
        }
        boundFramebuffer = inputFramebuffer
        return inputFramebuffer
    }

    @JvmStatic
    fun renderShadersToFramebuffer(shaders: Collection<BlitProgram>, framebuffer: Framebuffer) {
        val renderedFramebuffer = renderShaders(shaders)
        copyFramebuffer(renderedFramebuffer, framebuffer)
    }

    @JvmStatic
    fun copyFramebuffer(from: Framebuffer, to: Framebuffer, disableBlend: Boolean = true, copyDepth: Boolean = false) {
        blitFramebuffer(from, to, RenderPipelines.ENTITY_OUTLINE_BLIT, mapOf("InSampler" to from.colorTextureView))
        if (copyDepth) {
            copyDepthTexture(from, to)
        }
        boundFramebuffer = to
    }

    @JvmStatic
    fun copyDepthFramebuffer(from: Framebuffer, to: Framebuffer) {
        copyDepthTexture(from, to)
    }

    fun useFramebuffer(framebuffer: Framebuffer, action: () -> Unit) {
        val previousFramebuffer = sourceFramebuffer
        val previousBoundFramebuffer = boundFramebuffer
        sourceFramebuffer = framebuffer
        resetFramebuffers()
        copyFramebuffer(sourceFramebuffer, currentFramebuffer())
        nextFramebuffer()
        try {
            action()
        } finally {
            sourceFramebuffer = previousFramebuffer
            boundFramebuffer = previousBoundFramebuffer
        }
    }

    private fun renderShader(shader: BlitProgram, input: Framebuffer, output: Framebuffer) {
        val pass = shader.fragmentResourcePath()
            ?.toMatrixFragmentId()
            ?.let(::compiledSingleInputPass)

        if (pass == null) {
            blitFramebuffer(input, output, RenderPipelines.ENTITY_OUTLINE_BLIT, mapOf("InSampler" to input.colorTextureView))
            return
        }

        val textureBindings = pass.samplers.associateWith { input.colorTextureView }
        blitFramebuffer(input, output, pass.pipeline, textureBindings, pass)
    }

    private fun renderShader(shader: BlitProgram, output: Framebuffer, textureBindings: Map<String, Framebuffer>) {
        val fragmentShader = shader.fragmentResourcePath()?.toMatrixFragmentId()
        val pass = fragmentShader?.let(::compiledPostPass)

        if (pass == null || !pass.samplers.all(textureBindings::containsKey)) {
            val fallbackInput = textureBindings.values.firstOrNull() ?: boundFramebuffer
            blitFramebuffer(fallbackInput, output, RenderPipelines.ENTITY_OUTLINE_BLIT, mapOf("InSampler" to fallbackInput.colorTextureView))
            return
        }

        val firstInput = textureBindings.values.firstOrNull() ?: boundFramebuffer
        val textureViews = pass.samplers.associateWith { sampler ->
            val framebuffer = textureBindings.getValue(sampler)
            if (sampler.equals("depthAttachment", ignoreCase = true)) {
                framebuffer.depthTextureView ?: framebuffer.colorTextureView
            } else {
                framebuffer.colorTextureView
            }
        }
        blitFramebuffer(firstInput, output, pass.pipeline, textureViews, pass)
    }

    private fun compiledPostPass(fragmentShader: String): MatrixShaderPipelines.PostProcessPass? {
        val pass = MatrixShaderPipelines.postProcessPass(fragmentShader)
        if (!MatrixShaderPipelines.isPostPipelineCompiled(fragmentShader)) {
            MatrixShaderPipelines.precompilePostPipeline(fragmentShader)
        }
        return pass.takeIf { MatrixShaderPipelines.isPostPipelineCompiled(fragmentShader) }
    }

    private fun compiledSingleInputPass(fragmentShader: String): MatrixShaderPipelines.PostProcessPass? {
        val pass = compiledPostPass(fragmentShader) ?: return null
        return pass.takeIf { it.supportsSingleInput() }
    }

    private fun blitFramebuffer(
        input: Framebuffer,
        output: Framebuffer,
        pipeline: RenderPipeline,
        textureBindings: Map<String, GpuTextureView>,
        matrixPass: MatrixShaderPipelines.PostProcessPass? = null,
    ) {
        val outputTarget = output.renderTarget
        val outputColorView = outputTarget.colorTextureView ?: return
        val encoder = RenderSystem.getDevice().createCommandEncoder()
        val uniformBindings = matrixPass?.let { MatrixPostUniforms.prepare(it, input, output, encoder) } ?: emptyMap()
        val depthView = outputTarget.depthTextureView
        val renderPass = if (depthView != null) {
            encoder.createRenderPass(
                { "Matrix framebuffer blit" },
                outputColorView,
                OptionalInt.empty(),
                depthView,
                OptionalDouble.empty(),
            )
        } else {
            encoder.createRenderPass(
                { "Matrix framebuffer blit" },
                outputColorView,
                OptionalInt.empty(),
            )
        }

        renderPass.use { pass ->
            pass.setPipeline(pipeline)
            RenderSystem.bindDefaultUniforms(pass)
            val sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
            for ((name, textureView) in textureBindings) {
                pass.bindTexture(name, textureView, sampler)
            }
            for ((name, uniform) in uniformBindings) {
                pass.setUniform(name, uniform)
            }
            pass.draw(0, 3)
        }
    }

    private fun copyDepthTexture(from: Framebuffer, to: Framebuffer) {
        val sourceDepth = from.depthTexture ?: return
        val targetDepth = to.depthTexture ?: return
        RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
            sourceDepth,
            targetDepth,
            0,
            0,
            0,
            0,
            0,
            to.textureWidth.coerceAtMost(from.textureWidth),
            to.textureHeight.coerceAtMost(from.textureHeight),
        )
    }

    private fun String.toMatrixFragmentId(): String? {
        val normalized = replace('\\', '/')
            .removePrefix("/assets/matrix/shaders/")
            .removeSuffix(".fsh")
            .removeSuffix(".frag")
        return normalized.takeIf { it.startsWith("post/") }
    }
}
