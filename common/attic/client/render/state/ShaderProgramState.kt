/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.ShaderProgram
import java.util.function.Supplier

class ShaderProgramState(val program: Supplier<ShaderProgram?>) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): ShaderProgramState {
            val shader = RenderSystem.getShader()
            return ShaderProgramState { shader }
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        RenderSystem.setShader(program)

        return RenderPipelineSnapshot(snapshot)
    }
}