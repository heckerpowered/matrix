/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import net.minecraft.client.gl.Framebuffer

interface FramebufferExtension {
    companion object {
        @JvmStatic
        var framebufferColorFormat = 0

        fun <T> changeColorFormat(colorFormat: Int, action: () -> T): T {
            val previousColorFormat = framebufferColorFormat
            framebufferColorFormat = colorFormat
            val result = action()
            framebufferColorFormat = previousColorFormat
            return result
        }

        /**
         * Indicates whether mipmaps should be allocated for this [Framebuffer].
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
        var Framebuffer.allocateMipmaps: Boolean
            get() = (this as? FramebufferExtension)?.useMipmaps ?: false
            set(value) {
                (this as? FramebufferExtension)?.useMipmaps = value
            }

        /**
         *
         */
        fun Framebuffer.beginWriteLod(mipLevel: Int, attachment: Int = 0, setTextureSize: Boolean = true, setViewport: Boolean = true) {
            beginWrite(setViewport)
        }

        fun Framebuffer.endWriteLod() {
            beginWriteLod(0)
        }

        fun Framebuffer.beginReadLod(mipLevel: Int) {
        }

        fun Framebuffer.endReadLod() {
        }
    }

    var useMipmaps: Boolean
}
