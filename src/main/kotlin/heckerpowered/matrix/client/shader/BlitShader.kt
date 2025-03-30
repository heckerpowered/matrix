package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats

open class BlitShader(vertex: String, fragment: String, uniform: Array<UniformProvider> = emptyArray()) :
    Shader(vertex, fragment, uniform) {
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