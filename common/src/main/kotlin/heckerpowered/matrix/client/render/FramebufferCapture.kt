/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

object FramebufferCapture {
    private var capturing = false

    val captureFramebuffer = PostProcessRenderer.createManagedFramebuffer()

    fun beginCapture() {
        if (capturing) {
            throw IllegalStateException("Cannot begin capture while another capture is in progress")
        }
        capturing = true
    }

    fun endCapture() {
        capturing = false
    }
}
