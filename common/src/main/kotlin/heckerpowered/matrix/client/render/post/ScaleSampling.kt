/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import net.minecraft.client.gl.Framebuffer

object ScaleSampling {
    fun createManagedScalingFramebuffer(scaling: Double): Framebuffer = PostProcessRenderer.createManagedFramebuffer()
    fun getDownScalingFramebuffer(scaling: Double): Framebuffer = PostProcessRenderer.createManagedFramebuffer()
    fun getUpScalingFramebuffer(scaling: Double): Framebuffer = PostProcessRenderer.createManagedFramebuffer()
    fun sample(sourceFramebuffer: Framebuffer, targetFramebuffer: Framebuffer, sampler: BlitProgram) = Unit
}
