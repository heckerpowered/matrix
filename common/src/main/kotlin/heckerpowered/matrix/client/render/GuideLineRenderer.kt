/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.pipeline.BindGroupLayout
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.projectionMatrix
import heckerpowered.matrix.core.FramebufferExtension
import net.minecraft.util.ARGB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Optional
import java.util.OptionalDouble

/**
 * The aim-assist guide lines restored from 1.21's MatrixHud.renderCandidateEntities: world-space
 * lines from the player's chest to the targeted entity (green), then chained through the
 * aim-assist candidates (red), submitted in view space (camera-built matrix, CPU near-clip)
 * under the captured projectionMatrix, and drawn with depth test off so they stay visible
 * through walls.
 *
 * Mechanism mirrors the 1.21 original exactly (baseline renderCandidateEntities): DEBUG_LINES
 * with POSITION_COLOR — plain hardware 1px line rasterization, no width-quad expansion. The
 * expansion variant's screen-direction sign-flip degenerated view-dependently (segments
 * vanished/kinked at near-vertical angles), which hardware lines are immune to.
 *
 * The 1.21 pass drew with RenderSystem.setShaderColor(8,8,8,1) into the (then HDR)
 * hudFramebuffer, whose composite showed the lines over the backdrop blur and whose HDR copy
 * fed the HUD bloom. On 26.2 the same list is drawn twice: into the captured HUD framebuffer
 * at capture time ([renderToCapturedHud], where the 8x clamps exactly like the 1.21 HDR values
 * did on their way to the screen) and into an RGBA16F overlay whose unclamped 8x energy
 * [heckerpowered.matrix.client.render.post.BloomEffect] adds into the HUD brightness pass.
 */
object GuideLineRenderer {
    /** Vanilla near plane is 0.05; clip a hair beyond it so rasterization stays well-conditioned. */
    private const val NEAR_CLIP = 0.06F

    /** POSITION_COLOR: 3 floats + 4 unsigned bytes. */
    private const val VERTEX_SIZE = 16

    /** The 1.21 RenderSystem.setShaderColor(8,8,8,1) overbright multiplier. */
    private const val INTENSITY = 8.0F

    private class Segment(
        val x1: Float, val y1: Float, val z1: Float,
        val x2: Float, val y2: Float, val z2: Float,
        val color: Int,
    )

    private val segments = mutableListOf<Segment>()

    /**
     * True while the overlay target holds this frame's 8x line draw; gates the bloom feed so a
     * stale overlay is never composited (the overlay is neither cleared nor drawn on line-free
     * frames).
     */
    var overlayActive = false
        private set

    /** HDR bloom feed: the 8x overbright copy of the lines, added into BloomEffect's bright pass. */
    val overlayFramebuffer: RenderTarget by lazy {
        TextureTarget(
            "matrix guide line overlay",
            minecraft.window.width,
            minecraft.window.height,
            false,
            FramebufferExtension.framebufferColorFormat
        ).also(PostProcessRenderer::manageFramebuffer)
    }

    /**
     * Compiles both target-format pipeline variants against the ShaderManager sources; used
     * by the load-test probe so the shader cross-compilation is exercised on both backends
     * without needing an in-game aim target.
     */
    fun precompile() {
        val device = RenderSystem.getDevice()
        for (format in arrayOf(GpuFormat.RGBA8_UNORM, FramebufferExtension.framebufferColorFormat)) {
            device.precompilePipeline(pipeline(format)) { id, type ->
                minecraft.shaderManager.getShader(id, type)
            }
        }
    }

    fun clear() {
        segments.clear()
    }

    /** Records a segment during HUD extraction; positions are extraction-time lerped world coordinates. */
    fun addLine(from: Vec3, to: Vec3, color: Int) {
        segments.add(
            Segment(
                from.x.toFloat(), from.y.toFloat(), from.z.toFloat(),
                to.x.toFloat(), to.y.toFloat(), to.z.toFloat(),
                color
            )
        )
    }

    private val pipelines = mutableMapOf<GpuFormat, RenderPipeline>()

    private fun pipeline(format: GpuFormat): RenderPipeline = pipelines.getOrPut(format) {
        RenderPipeline.builder()
            .withLocation(Matrix.identifier("pipeline/guide_line_${format.name.lowercase()}"))
            .withVertexShader(Matrix.identifier("core/guide_line"))
            .withFragmentShader(Matrix.identifier("core/guide_line"))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            // DEBUG_LINES = real GPU line-list rasterization (the 1.21 mechanism); LINES is
            // vanilla's width-quad expansion variant whose GPU primitives are TRIANGLES —
            // a non-indexed 2-vertex-per-segment draw renders nothing under it.
            .withPrimitiveTopology(PrimitiveTopology.DEBUG_LINES)
            .withCull(false)
            // 1.21 state: depth test off (lines visible through walls), blend off — no
            // depth-stencil state and no blend function baked into the pipeline.
            .withDepthStencilState(Optional.empty())
            .withColorTargetState(ColorTargetState(Optional.empty(), format, ColorTargetState.WRITE_ALL))
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withUniform("GuideLineTransform", UniformType.UNIFORM_BUFFER)
                    .build()
            )
            .build()
    }

    private var vertexBuffer: GpuBuffer? = null

    private fun uploadVertices(data: ByteBuffer): GpuBufferSlice {
        val size = data.remaining().toLong()
        var buffer = vertexBuffer
        if (buffer == null || buffer.size() < size) {
            // Growth only ever replaces a buffer whose last GPU use was a previous frame.
            buffer?.close()
            buffer = RenderSystem.getDevice().createBuffer(
                { "matrix guide line vertices" },
                GpuBuffer.USAGE_VERTEX or GpuBuffer.USAGE_COPY_DST,
                size
            )
            vertexBuffer = buffer
        }
        val slice = buffer.slice(0, size)
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(slice, data)
        return slice
    }

    /** Ring of UBO slots — the block is written twice per frame (main + overlay draw). */
    private val uniformBuffer: GpuBuffer by lazy {
        RenderSystem.getDevice().createBuffer(
            { "matrix guide line uniforms" },
            GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_COPY_DST,
            UNIFORM_SLOT_SIZE * UNIFORM_SLOTS
        )
    }
    private var uniformCursor = 0

    private const val UNIFORM_SLOT_SIZE = 256L
    private const val UNIFORM_SLOTS = 16

    private fun writeUniform(target: RenderTarget, intensity: Float): GpuBufferSlice {
        // Vertices are submitted in VIEW space (camera-relative, near-clipped on the CPU in
        // render()), so the shader's ViewProjMat is the projection alone.
        val data = ByteBuffer.allocateDirect(UNIFORM_SLOT_SIZE.toInt()).order(ByteOrder.nativeOrder())
        val written = Std140Builder.intoBuffer(data)
            .putMat4f(projectionMatrix)
            .putVec4(target.width.toFloat(), target.height.toFloat(), intensity, 0.0F)
            .get()
        val slice = uniformBuffer.slice(UNIFORM_SLOT_SIZE * uniformCursor, UNIFORM_SLOT_SIZE)
        uniformCursor = (uniformCursor + 1) % UNIFORM_SLOTS
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(slice, written)
        return slice
    }

    /**
     * Draws the extracted line list. Called from GameRendererMixin.beginRender right AFTER
     * PostProcessRenderer.renderToMinecraftFramebuffer(), matching 1.21's InGameHud-time draw:
     * the lines stay fully colored while the slow-time grayscale desaturates the world.
     */
    @JvmStatic
    fun render() {
        overlayActive = false
        pendingVertices = null
        // Hud.isHidden (F1) suppressed the whole 1.21 HUD callback, lines included; the extracted
        // list can be stale on hidden-HUD frames because extraction is skipped, so gate here too.
        if (segments.isEmpty() || minecraft.gui.hud.isHidden) {
            return
        }

        // Vertices go through the pipeline in VIEW space, near-clipped here on the CPU. The
        // 1.21 draw was plain GL_LINES whose hardware clip handled the chest-anchored start
        // vertex sitting AT the camera plane; the 26.2 width-quad expansion divides by w in
        // the vertex shader instead, so an unclipped w≈0 endpoint turns into NaN/flipped NDC
        // (invisible segments at best, the full-screen tangle at worst). Clipping against the
        // near plane before submission restores the hardware-clip visuals — and camera-
        // relative coordinates dodge the float precision loss of far-from-origin worlds.
        val camera = minecraft.gameRenderer.mainCamera()
        val view = Matrix4f()
            .rotate(camera.rotation().conjugate(Quaternionf()))
            .translate(
                -camera.position().x.toFloat(),
                -camera.position().y.toFloat(),
                -camera.position().z.toFloat(),
            )
        val p1 = Vector3f()
        val p2 = Vector3f()

        // Plain POSITION_COLOR line-list vertices (two per segment, no duplication): the
        // 1.21 draw was DEBUG_LINES through Tessellator — hardware line rasterization.
        val data = stagingBuffer(VERTEX_SIZE * segments.size * 2)
        for (segment in segments) {
            view.transformPosition(segment.x1, segment.y1, segment.z1, p1)
            view.transformPosition(segment.x2, segment.y2, segment.z2, p2)
            // View space looks down -z; keep only the part beyond the near plane. The clip
            // parameter is clamped and the results finite-checked: during fast camera turns
            // a segment can sweep THROUGH the camera plane within one frame, and a near-zero
            // denominator would fling a vertex toward infinity — one giant 8x streak through
            // the bloom chain reads as a full-screen white flash.
            if (p1.z > -NEAR_CLIP && p2.z > -NEAR_CLIP) {
                continue
            }
            if (p1.z > -NEAR_CLIP) {
                p1.lerp(p2, ((-NEAR_CLIP - p1.z) / (p2.z - p1.z)).coerceIn(0F, 1F))
            } else if (p2.z > -NEAR_CLIP) {
                p2.lerp(p1, ((-NEAR_CLIP - p2.z) / (p1.z - p2.z)).coerceIn(0F, 1F))
            }
            if (!p1.isFinite || !p2.isFinite) {
                continue
            }
            putVertex(data, p1, segment.color)
            putVertex(data, p2, segment.color)
        }
        data.flip()
        if (!data.hasRemaining()) {
            return
        }

        val vertices = uploadVertices(data)
        val vertexCount = data.limit() / VERTEX_SIZE

        // Only the HDR bloom feed is drawn here. The VISIBLE lines were 1.21 HUD content
        // (drawn while hudFramebuffer was bound, composited OVER the list's backdrop blur),
        // so they join the capture in renderToCapturedHud instead — a main-target draw at
        // this point would sit UNDER the backdrop blur and come out dark and smeared
        // whenever the magic list is open.
        PostProcessRenderer.clear(overlayFramebuffer)
        drawTo(overlayFramebuffer, vertices, vertexCount)
        overlayActive = true

        pendingVertices = vertices
        pendingVertexCount = vertexCount
    }

    private fun putVertex(data: ByteBuffer, position: Vector3f, color: Int) {
        data.putFloat(position.x).putFloat(position.y).putFloat(position.z)
        // POSITION_COLOR stores RGBA unsigned bytes; the segment color is ARGB.
        data.put(ARGB.red(color).toByte())
            .put(ARGB.green(color).toByte())
            .put(ARGB.blue(color).toByte())
            .put(ARGB.alpha(color).toByte())
    }

    /** CPU staging for the per-frame vertex pack; grown on demand, reused across frames. */
    private var staging: ByteBuffer? = null

    private fun stagingBuffer(capacity: Int): ByteBuffer {
        val buffer = staging?.takeIf { it.capacity() >= capacity }
            ?: ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder()).also { staging = it }
        return buffer.clear()
    }

    private var pendingVertices: GpuBufferSlice? = null
    private var pendingVertexCount = 0

    /**
     * Draws this frame's line list into the captured HUD framebuffer — called from
     * MatrixHud.onHudCaptureBegin (after the capture clear, before the HUD stratum renders),
     * restoring the 1.21 layer order: lines above the backdrop blur, below the list panels.
     * Like 1.21 (where the lines lived in hudFramebuffer), they are only visible on frames
     * where the HUD composite actually runs.
     */
    @JvmStatic
    fun renderToCapturedHud(target: RenderTarget) {
        val vertices = pendingVertices ?: return
        drawTo(target, vertices, pendingVertexCount)
    }

    private fun drawTo(
        target: RenderTarget,
        vertices: GpuBufferSlice,
        vertexCount: Int,
    ) {
        val colorView = target.colorTextureView ?: return
        val device = RenderSystem.getDevice()
        val pipeline = pipeline(target.colorTexture?.format ?: GpuFormat.RGBA8_UNORM)
        // Same lazy compile as BlitProgram: mod pipelines are unknown to the device's default
        // shader source, so compile against the ShaderManager-loaded sources (cached lookup).
        device.precompilePipeline(pipeline) { id, type ->
            minecraft.shaderManager.getShader(id, type)
        }

        // The captured HUD target's 8x clamps to the same values the 1.21 HDR draw showed on
        // screen; the overlay keeps them unclamped for the bloom feed.
        val uniformSlice = writeUniform(target, INTENSITY)

        device.createCommandEncoder().createRenderPass(
            { "matrix guide lines" },
            colorView,
            Optional.empty(),
            null,
            OptionalDouble.empty()
        ).use { pass ->
            pass.setPipeline(pipeline)
            RenderSystem.bindDefaultUniforms(pass)
            pass.setUniform("GuideLineTransform", uniformSlice)
            pass.setVertexBuffer(0, vertices)
            pass.draw(vertexCount, 1, 0, 0)
        }
    }
}
