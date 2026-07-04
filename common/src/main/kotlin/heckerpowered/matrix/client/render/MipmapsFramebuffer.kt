/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.core.FramebufferExtension
import org.joml.Vector4f
import org.joml.Vector4fc
import java.util.Optional
import java.util.OptionalDouble

/**
 * A [RenderTarget] whose color attachment owns a full mipmap chain, and which can be pointed
 * at an individual level via [levelOfDetail] for both rendering and sampling.
 *
 * 26.2 note: [RenderTarget.createBuffers] normally allocates a single-level `RGBA8_UNORM`
 * color texture (and a `D32_FLOAT` depth texture) via [com.mojang.blaze3d.systems.GpuDevice].
 * There is no `initFbo`/`glTexImage2D`/`glGenerateMipmap` left to intercept, so this class
 * overrides [createBuffers] directly: it allocates the color texture with
 * [recommendMipLevel] levels and [FramebufferExtension.framebufferColorFormat], then creates
 * one single-level [GpuTextureView] per mip so [levelOfDetail] can retarget
 * [colorTextureView] (the field every draw/sample path in the wrapper API reads).
 *
 * GPU-side mip regeneration (the old `glGenerateMipmap`) has no wrapper equivalent — see the
 * `TODO(26.2)` below. Levels above 0 stay whatever a caller last rendered into them.
 */
open class MipmapsFramebuffer(label: String, width: Int, height: Int, useDepth: Boolean) :
    RenderTarget(label, useDepth, FramebufferExtension.framebufferColorFormat) {
    /** One single-level view per mip, index = mip level. Rebuilt in [createBuffers]. */
    private var mipViews: Array<GpuTextureView> = emptyArray()

    init {
        resize(width, height)
    }

    @Suppress("NAME_SHADOWING")
    override fun createBuffers(width: Int, height: Int) {
        RenderSystem.assertOnRenderThread()

        // Callers may resize before the window dimensions are known (e.g. static init);
        // the wrapper API rejects 0-sized textures, so clamp like the vanilla targets do.
        val width = width.coerceAtLeast(1)
        val height = height.coerceAtLeast(1)

        val device = RenderSystem.getDevice()
        // recommendMipLevel's bit trick yields 0 for a 1x1 texture; the wrapper requires >= 1.
        val mipLevels = recommendMipLevel(width, height).coerceAtLeast(1)

        if (useDepth) {
            val depth = device.createTexture(
                { "$label depth texture" },
                GpuTexture.USAGE_RENDER_ATTACHMENT or GpuTexture.USAGE_TEXTURE_BINDING
                        or GpuTexture.USAGE_COPY_DST or GpuTexture.USAGE_COPY_SRC,
                GpuFormat.D32_FLOAT, width, height, 1, 1
            )
            depthTexture = depth
            depthTextureView = device.createTextureView(depth)
        }

        val color = device.createTexture(
            { "$label color texture" },
            GpuTexture.USAGE_COPY_DST or GpuTexture.USAGE_COPY_SRC or GpuTexture.USAGE_TEXTURE_BINDING or GpuTexture.USAGE_RENDER_ATTACHMENT,
            FramebufferExtension.framebufferColorFormat, width, height, 1, mipLevels
        )
        colorTexture = color

        mipViews = Array(mipLevels) { level -> device.createTextureView(color, level, 1) }
        colorTextureView = mipViews[0]
        levelOfDetail = 0
    }

    /**
     * The mip level currently exposed through [colorTextureView] (and therefore through every
     * draw-to/sample-from path that reads it, matching the old begin/end-write/read-lod pair
     * which repointed the same GL attachment).
     */
    var levelOfDetail: Int = 0
        set(value) {
            val clamped = value.coerceIn(0, mipViews.lastIndex.coerceAtLeast(0))
            field = clamped
            if (mipViews.isNotEmpty()) {
                colorTextureView = mipViews[clamped]
            }
        }

    /**
     * The single-level [GpuTextureView] for [level], independent of [levelOfDetail].
     *
     * Needed by callers (e.g. [heckerpowered.matrix.client.render.post.BloomEffect]'s upsample
     * pass) that must read one mip level while [levelOfDetail] (and therefore [colorTextureView])
     * is pointed at a *different* level for writing — the old GL implementation aliased
     * independent `GL_READ_FRAMEBUFFER`/`GL_DRAW_FRAMEBUFFER` attachment points onto the same
     * texture object to achieve this; this is the wrapper-API equivalent for read-only sampling.
     */
    fun viewAt(level: Int): GpuTextureView {
        return mipViews[level.coerceIn(0, mipViews.lastIndex.coerceAtLeast(0))]
    }

    /**
     * Clears every mip level (and the depth attachment) to transparent black, restoring the
     * baseline's per-level attach-then-clear loop: the 26.2 encoder clear calls only ever
     * target mip 0 (GlTexture.fboMipLevel() is hardwired to 0), so levels >= 1 must be cleared
     * through a load-op clear render pass on their single-level views instead.
     */
    fun clearAllLevels() {
        RenderSystem.assertOnRenderThread()
        // Level 0 + depth take the same encoder path as every other target.
        PostProcessRenderer.clear(this)
        val encoder = RenderSystem.getDevice().createCommandEncoder()
        for (level in 1 until mipViews.size) {
            encoder.createRenderPass(
                { "$label clear mip $level" },
                mipViews[level],
                TRANSPARENT_CLEAR,
                null,
                OptionalDouble.empty()
            ).close()
        }
    }

    private companion object {
        val TRANSPARENT_CLEAR: Optional<Vector4fc> = Optional.of(Vector4f(0F, 0F, 0F, 0F))
    }

    override fun destroyBuffers() {
        // Vanilla destroyBuffers only closes colorTexture/colorTextureView/depth*; without
        // this the per-mip views (all but the one aliased by colorTextureView) leak on every
        // resize. GlTextureView.close() is idempotent, so the aliased view closing again in
        // super is harmless.
        for (view in mipViews) {
            view.close()
        }
        mipViews = emptyArray()
        super.destroyBuffers()
    }

    // TODO(26.2): there is no GpuDevice/CommandEncoder call that regenerates a mip chain from
    // level 0 (the old `glGenerateMipmap(fbo)` in this class's init, and the box-filter downsample
    // callers used to do manually via BloomEffect's per-level render passes still works exactly
    // as before since it renders explicitly into each mip's GpuTextureView -- only *automatic*
    // GPU-side mipmap generation from a single base-level write has no wrapper equivalent).
}
