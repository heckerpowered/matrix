/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.core

import net.minecraft.client.gl.Framebuffer

object FramebufferSpoof {
    private val stack = mutableListOf<Framebuffer>()

    private var spoofedFramebuffer: Framebuffer? = null

    @JvmStatic
    fun getSpoofedFramebuffer(): Framebuffer? {
        return spoofedFramebuffer
    }

    fun push(spoofedFramebuffer: Framebuffer) {
        FramebufferSpoof.spoofedFramebuffer?.let {
            stack.addLast(it)
        }

        FramebufferSpoof.spoofedFramebuffer = spoofedFramebuffer
        spoofedFramebuffer.beginWrite(false)
    }

    fun pop() {
        if (stack.isEmpty()) {
            spoofedFramebuffer = null
            return
        }

        spoofedFramebuffer = stack.last()
    }

    fun clear() {
        stack.clear()
        spoofedFramebuffer = null
    }
}