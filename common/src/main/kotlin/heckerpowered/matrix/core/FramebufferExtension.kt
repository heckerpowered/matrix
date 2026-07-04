/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.pipeline.RenderTarget
import heckerpowered.matrix.client.render.MipmapsFramebuffer

/**
 * HDR framebuffer support surface, implemented via Mixin onto [RenderTarget].
 *
 * On the 26.2 GpuDevice wrapper API, [RenderTarget.createBuffers] always allocates the color
 * attachment as `GpuFormat.RGBA8_UNORM`. [framebufferColorFormat] records the format Matrix
 * *wants* new framebuffers to use (read by [MipmapsFramebuffer] and by whatever
 * allocation path a Mixin end up driving); it no longer patches a raw GL image-format
 * constant since there is nothing resembling `initFbo`/`glTexImage2D` left to intercept.
 */
interface FramebufferExtension {
    companion object {
        @JvmStatic
        var framebufferColorFormat: GpuFormat = GpuFormat.RGBA16_FLOAT

        fun <T> changeColorFormat(colorFormat: GpuFormat, action: () -> T): T {
            val previousColorFormat = framebufferColorFormat
            framebufferColorFormat = colorFormat
            val result = action()
            framebufferColorFormat = previousColorFormat
            return result
        }

        /**
         * Indicates whether mipmaps should be allocated for this [RenderTarget].
         *
         * When set to `true`, the framebuffer's color attachment will be initialized
         * with enough storage to hold a full mipmap chain, allowing rendering to or sampling
         * from individual mipmap levels. This is particularly useful for effects such as
         * bloom, where lower-resolution versions of the framebuffer are needed.
         *
         * After changing this property, the framebuffer must be re-initialized
         * to allocate storage for the mipmap chain. Mipmap storage cannot be allocated during construction.
         *
         * This property relies on Mixin. If the Mixin is not properly initialized,
         * accessing this property will always return `false`, and setting it will have no effect.
         * No exceptions will be thrown in such cases.
         */
        var RenderTarget.allocateMipmaps: Boolean
            get() = (this as? FramebufferExtension)?.useMipmaps ?: false
            set(value) {
                (this as? FramebufferExtension)?.useMipmaps = value
            }

        /**
         * Points this [RenderTarget] at mip level [mipLevel] for both writing (rendering) and
         * reading (sampling), then narrows the effective viewport to that level's dimensions.
         *
         * 26.2 note: the old immediate-mode `beginWrite`/`glFramebufferTexture2D`/`glViewport`
         * sequence has no equivalent — render passes are self-contained and target an explicit
         * [com.mojang.blaze3d.textures.GpuTextureView] instead of a bound FBO attachment. This
         * only has a real implementation for [MipmapsFramebuffer], which owns a per-level
         * [com.mojang.blaze3d.textures.GpuTextureView] array; for any other [RenderTarget] this
         * is a no-op.
         */
        fun RenderTarget.beginWriteLod(mipLevel: Int, setViewport: Boolean = true) {
            (this as? MipmapsFramebuffer)?.levelOfDetail = mipLevel
        }

        fun RenderTarget.endWriteLod() {
            beginWriteLod(0)
        }

        fun RenderTarget.beginReadLod(mipLevel: Int) {
            (this as? MipmapsFramebuffer)?.levelOfDetail = mipLevel
        }

        fun RenderTarget.endReadLod() {
            (this as? MipmapsFramebuffer)?.levelOfDetail = 0
        }
    }

    var useMipmaps: Boolean
}