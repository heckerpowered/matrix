/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.shader.component.ShaderComponent
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats

open class BlitProgram(
    vararg shaders: Shader,
    uniforms: Array<UniformProvider> = emptyArray(),
    uniformBuffers: Array<UniformBufferProvider> = emptyArray(),
    components: Array<ShaderComponent> = emptyArray(),
) :
    Program(*shaders, uniforms = uniforms, uniformBuffers = uniformBuffers, components = components) {
    companion object {
        private var buffer = VertexBuffer(VertexBuffer.Usage.DYNAMIC)

        init {
            val builder = Tessellator.getInstance()
            val buffer = builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
            buffer.vertex(-1F, -1F, 0F).texture(0F, 0F)
            buffer.vertex(1F, -1F, 0F).texture(1F, 0F)
            buffer.vertex(1F, 1F, 0F).texture(1F, 1F)
            buffer.vertex(-1F, 1F, 0F).texture(0F, 1F)
            this.buffer.bind()
            this.buffer.upload(buffer.end())
            VertexBuffer.unbind()
        }

        fun blit() {
            buffer.bind()
            buffer.draw()
            VertexBuffer.unbind()
        }
    }

    fun blit() {
        RenderSystem.disableBlend()
        enableShader()
        buffer.bind()
        buffer.draw()
        VertexBuffer.unbind()
        disableShader()
        RenderSystem.enableBlend()
    }
}