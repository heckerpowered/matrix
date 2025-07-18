package heckerpowered.matrix.client.render.state

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.ShaderProgram
import java.util.function.Supplier

class MinecraftShaderState(val program: Supplier<ShaderProgram?>) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): MinecraftShaderState {
            val shader = RenderSystem.getShader()
            return MinecraftShaderState { shader }
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        RenderSystem.setShader(program)

        return RenderPipelineSnapshot(snapshot)
    }
}