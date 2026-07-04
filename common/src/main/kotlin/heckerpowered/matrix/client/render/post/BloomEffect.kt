/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.event.InitAttachmentCallback
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.GuideLineRenderer
import heckerpowered.matrix.client.render.MipmapsFramebuffer
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.mainRenderTarget
import heckerpowered.matrix.client.render.recommendMipLevel
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.FramebufferExtension
import heckerpowered.matrix.core.FramebufferExtension.Companion.allocateMipmaps
import heckerpowered.matrix.core.FramebufferExtension.Companion.beginReadLod
import heckerpowered.matrix.core.FramebufferExtension.Companion.beginWriteLod
import org.slf4j.MarkerFactory

object BloomEffect {
    private val MARKER = MarkerFactory.getMarker("BLOOM_RENDERER")

    // 1.21's bright pass was HDR (the framebuffer mixin made every mod FBO RGBA16F). This
    // target only ever receives BlitProgram output (never vanilla RGBA8-declared pipelines),
    // so unlike createManagedFramebuffer it can keep the HDR format on 26.2 — required for the
    // guide-line overlay's 8x energy to survive into the (already HDR) mip chain below.
    private val brightFramebuffer: RenderTarget = TextureTarget(
        "matrix bloom bright",
        minecraft.window.width,
        minecraft.window.height,
        true,
        FramebufferExtension.framebufferColorFormat
    ).also(PostProcessRenderer::manageFramebuffer)

    /**
     * Source framebuffer + LOD for [bloomTentBlurShader]'s `framebuffer` sampler.
     *
     * The shared `tentBlurShader` in RenderExtensions.kt reads a module-private
     * `primaryFramebuffer` that only the `blend`/`copyTo`-style infix helpers in that file can
     * set, so bloom keeps its own tent-blur program instance (mirroring the old dedicated
     * `TentShader` object) with locally settable source state instead.
     */
    // Explicit per-level source view: the old GL chain attached READ (level n) and DRAW
    // (level m) FBOs independently; MipmapsFramebuffer's shared levelOfDetail view can only
    // point at one level, so the tent samples an explicit viewAt() while the shared view
    // stays on the write level.
    private var tentSourceView: GpuTextureView? = null
    private var tentLevelOfDetail = 0F

    private val bloomTentBlurShader by lazy {
        BlitProgram(
            "post/blur/tent.fsh",
            uniforms = arrayOf(
                UniformProvider("MatrixPostUniforms") {
                    // MatrixPostData0.x = lod
                    putVec4(tentLevelOfDetail, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                }
            ),
            textures = arrayOf(
                TextureProvider("framebuffer", bilinear = true) { tentSourceView }
            )
        )
    }

    // TODO(26.2): verify against final MipmapsFramebuffer API. Old code used a single managed
    // RenderTarget plus manual per-level FBO attachment redirection (beginWriteLod/beginReadLod
    // against raw GL). MipmapsFramebuffer owns a real per-level GpuTextureView chain and exposes
    // a settable `levelOfDetail` that beginWriteLod/beginReadLod (see FramebufferExtension.kt)
    // already redirect to when the target is a MipmapsFramebuffer, so those helper calls below
    // are kept as-is and now have real per-level effect instead of being GL no-ops.
    // Window dimensions, NOT mainRenderTarget's: this object initializes during mod init,
    // when the main render target still reports 1x1 — and with no window-resize event ever
    // firing afterwards on a fixed-size session, the whole bloom pyramid stayed 1x1 (the
    // composite then painted a uniform fullscreen tint instead of a halo). brightFramebuffer
    // above already used the window for exactly this reason.
    val bloomDownFramebuffer = MipmapsFramebuffer(
        "matrix bloom down", minecraft.window.width, minecraft.window.height, true
    )
    val bloomUpFramebuffer = MipmapsFramebuffer(
        "matrix bloom up", minecraft.window.width, minecraft.window.height, true
    )

    init {
        InitAttachmentCallback.EVENT.register(::onInitAttachment)
        PostProcessRenderer.manageFramebuffer(bloomDownFramebuffer)
        PostProcessRenderer.manageFramebuffer(bloomUpFramebuffer)

        bloomDownFramebuffer.resize(bloomDownFramebuffer.width, bloomDownFramebuffer.height)
        bloomUpFramebuffer.resize(bloomDownFramebuffer.width, bloomDownFramebuffer.height)
    }

    private fun onInitAttachment(framebuffer: RenderTarget) {
        if (framebuffer != bloomDownFramebuffer && framebuffer != bloomUpFramebuffer) {
            return
        }

        // TODO(26.2): FramebufferExtension.allocateMipmaps pending port — MipmapsFramebuffer
        // always allocates its full mip chain in createBuffers(), so this flag is redundant for
        // bloomDown/bloomUpFramebuffer specifically, but kept for source parity / Mixin callers.
        framebuffer.allocateMipmaps = true

        // TODO(26.2): texture filter mode (linear) no longer settable via direct GL call;
        // TextureProvider(bilinear=true) on the SAMPLING side achieves the same visual effect
        // for reads of this texture — verify all TextureProviders reading bloomDownFramebuffer/
        // bloomUpFramebuffer pass bilinear=true (done below: brightnessShader's own read is via
        // the tentBlurShader/copyFramebuffer paths, both of which already sample bilinear=true
        // per RenderExtensions.kt's shared tentBlurShader definition).
    }

    var brightnessPassFramebuffer: RenderTarget = minecraft.mainRenderTarget
    var brightnessThreshold = 0F
    var bloomIntensity = 1.0F

    /**
     * True only around MatrixHud's end-of-HUD bloom pass. In 1.21 the 8x guide-line energy
     * lived inside hudFramebuffer/blurFramebuffer (every mod FBO was RGBA16F) and therefore
     * only ever entered THAT bloom (threshold 1) — never the world/screen-effect passes,
     * which run before the HUD callback draws the lines. This flag keeps that scoping so the
     * glow is neither double-added nor visible on passes that never saw it pre-migration.
     */
    var includeGuideLineOverlay = false

    /**
     * Copies an explicit mip level of [bloomUpFramebuffer] into whatever level
     * [bloomUpFramebuffer]'s `levelOfDetail` currently points at.
     *
     * [MipmapsFramebuffer] exposes a single `colorTextureView` selected by [levelOfDetail], so
     * [PostProcessRenderer.copyFramebuffer] cannot alias two different levels of the same
     * framebuffer as source and destination (the write-side `beginWriteLod` call would repoint
     * the read-side view too). This reads an explicit level via [MipmapsFramebuffer.viewAt]
     * instead, replacing the old GL code's independent `GL_READ_FRAMEBUFFER`/
     * `GL_DRAW_FRAMEBUFFER` attachment aliasing trick.
     */
    private var upsampleCopySourceLevel = 0
    private val upsampleCopyShader by lazy {
        BlitProgram(
            "blit/blit_no_depth.fsh",
            uniforms = arrayOf(UniformProvider("BlitConfig") { putFloat(0F) }),
            textures = arrayOf(
                // bilinear: the old chain set GL_LINEAR min/mag on bloomUp, so the 2x
                // level-to-level upscale interpolated — NEAREST here makes the halo blocky.
                TextureProvider("framebuffer", bilinear = true) { bloomUpFramebuffer.viewAt(upsampleCopySourceLevel) }
            )
        )
    }

    private val brightnessShader by lazy {
        BlitProgram(
            "post/bloom/bloom_brightness_pass.fsh",
            uniforms = arrayOf(
                UniformProvider("MatrixPostUniforms") {
                    // MatrixPostData0 = vec4(bloomThreshold, bloomIntensity, 0, 0)
                    putVec4(brightnessThreshold, bloomIntensity, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                }
            ),
            textures = arrayOf(
                TextureProvider("framebuffer", bilinear = true) { brightnessPassFramebuffer.colorTextureView }
            )
        )
    }

    private fun clearBloomPasses() {
        PostProcessRenderer.clear(brightFramebuffer)

        // The baseline loop re-attached the FBO per level before each clear; the 26.2 encoder
        // clears only ever hit mip 0 (levels >= 1 would accumulate additive upsample energy
        // frame over frame), so the per-level clears live in MipmapsFramebuffer.clearAllLevels.
        bloomDownFramebuffer.clearAllLevels()
        bloomUpFramebuffer.clearAllLevels()
    }

    private fun computeBloomPass() {
        PostProcessRenderer.renderShaderToFramebuffer(brightnessShader, brightFramebuffer)
        // Guide lines: 1.21 fed bloom by drawing them 8x overbright into the (HDR) HUD
        // framebuffer, whose blurFramebuffer copy this brightness pass reads during the HUD
        // composite; on 26.2 that energy lives in the RGBA16F guide-line overlay instead,
        // added on top of the brightness output (GL_ONE/GL_ONE, the old additive composite)
        // so it enters the mip bloom chain — HUD pass only, see includeGuideLineOverlay.
        if (includeGuideLineOverlay && GuideLineRenderer.overlayActive) {
            PostProcessRenderer.copyFramebuffer(GuideLineRenderer.overlayFramebuffer, brightFramebuffer, BlendFunction.ADDITIVE)
        }
    }

    private fun prepareDownsamplePass() {
        bloomDownFramebuffer.beginWriteLod(0)
        PostProcessRenderer.copyFramebuffer(brightFramebuffer, bloomDownFramebuffer)
    }

    private fun generateDownsamplePasses(mipLevel: Int) {
        prepareDownsamplePass()

        for (i in 1..<mipLevel) {
            // Sample level i-1 through an explicit view while the shared view points at the
            // write level. (A beginReadLod(i-1) here used to clobber beginWriteLod(i)'s
            // level selection — every pass then rendered into i-1 while sampling i-1, the
            // pyramid never built past level 0, and the bloom halo collapsed to a tiny
            // tent soften.)
            tentLevelOfDetail = i - 1.0F
            tentSourceView = bloomDownFramebuffer.viewAt(i - 1)
            bloomDownFramebuffer.beginWriteLod(i)
            PostProcessRenderer.renderShaderToFramebuffer(bloomTentBlurShader, bloomDownFramebuffer)
        }
    }

    private fun prepareUpsamplePass(mipLevel: Int) {
        PostProcessRenderer.levelOfDetail = mipLevel - 1.0F
        bloomDownFramebuffer.beginReadLod(mipLevel - 1)
        bloomUpFramebuffer.beginWriteLod(mipLevel - 1)
        PostProcessRenderer.copyFramebuffer(bloomDownFramebuffer, bloomUpFramebuffer)
    }

    private fun generateUpsamplePasses(mipLevel: Int) {
        prepareUpsamplePass(mipLevel)
        for (i in (0..<(mipLevel - 1)).reversed()) {
            // Copy LOD(n+1) to LOD(n). Reads an explicit mip level via upsampleCopyShader
            // (MipmapsFramebuffer.viewAt) rather than PostProcessRenderer.copyFramebuffer, since
            // both source and destination are the same framebuffer object here and
            // copyFramebuffer's TextureProvider reads the shared, levelOfDetail-selected
            // colorTextureView — beginWriteLod(i) below would otherwise repoint the read side too
            // (see upsampleCopyShader's doc comment). This replaces the old GL code's independent
            // GL_READ_FRAMEBUFFER/GL_DRAW_FRAMEBUFFER attachment aliasing trick.
            PostProcessRenderer.levelOfDetail = i + 1.0F
            upsampleCopySourceLevel = i + 1
            bloomUpFramebuffer.beginWriteLod(i)
            PostProcessRenderer.renderShaderToFramebuffer(upsampleCopyShader, bloomUpFramebuffer, BlendFunction.ADDITIVE)

            // Old stagger: at up-level i the tent sampled bloomDown lod i+1 (TentShader's
            // levelOfDetail was seeded to mip-1 BEFORE this loop and only advanced to i
            // AFTER each render — that trailing assignment was the stagger, not vestigial).
            // Sampling level i here instead doubled the sharp full-res core into the sum
            // and dropped the blurriest pyramid term.
            tentLevelOfDetail = (i + 1).toFloat()
            tentSourceView = bloomDownFramebuffer.viewAt(i + 1)
            bloomUpFramebuffer.beginWriteLod(i)
            PostProcessRenderer.renderShaderToFramebuffer(bloomTentBlurShader, bloomUpFramebuffer, BlendFunction.ADDITIVE)
        }
    }

    private fun resetBloomPasses() {
        bloomUpFramebuffer.beginReadLod(0)
        bloomUpFramebuffer.beginWriteLod(0)
        // The up-chain drives the SHARED blit lod (old TentShader had its own uniform; the
        // 26.2 port funnels it through PostProcessRenderer.levelOfDetail, read by EVERY
        // blit/copy shader) — without this reset the final bloomUp->main composite and every
        // later copy in the session samples its source at a stale deep LOD.
        PostProcessRenderer.levelOfDetail = 0F
    }

    private fun generateMipmaps(mipLevel: Int) {
        generateDownsamplePasses(mipLevel)
        generateUpsamplePasses(mipLevel)
    }

    fun renderBloom() {
        val mipLevel = minecraft.mainRenderTarget.recommendMipLevel()
        clearBloomPasses()
        computeBloomPass()
        generateMipmaps(mipLevel)
        resetBloomPasses()
    }

    var renderBloom = false
}