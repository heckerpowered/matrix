/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
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