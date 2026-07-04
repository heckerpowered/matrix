/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.render.PostProcessRenderer
import com.mojang.blaze3d.pipeline.RenderTarget

// 26.2: this renderer was already dead code before the port (every step below was commented out
// except raw GL mip-generation calls with no GpuDevice equivalent - manual glTexStorage2D /
// glFramebufferTexture2D mip binding has no 1:1 wrapper API surface; mip management now belongs to
// MipmapsFramebuffer). No callers exist anywhere in common/ (verified). Left as an inert stub rather
// than deleted, to preserve the public shape for whatever this was meant to become.
// TODO(26.2): reimplement on top of MipmapsFramebuffer if this renderer is revived.
object StandardBloomRenderer {
    private val framebuffer: RenderTarget by lazy {
        PostProcessRenderer.createManagedFramebuffer()
    }

    fun render(brightnessPass: RenderTarget) {
        // TODO(26.2): mip-chain generation/binding previously done via raw GL calls
        // (glTexStorage2D/glFramebufferTexture2D/glGenerateMipmap) has no wrapper-API
        // equivalent here; this call is currently a no-op pending a MipmapsFramebuffer-based
        // reimplementation.
    }
}