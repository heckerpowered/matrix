/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader.sdf

import com.mojang.blaze3d.platform.GlConst
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector2f
import org.joml.Vector4f
import org.lwjgl.opengl.GL46.*

object DropShadowRenderer {
    /**
     * The color attachment of the signed distance field.
     */
    var signedDistanceField: Int = -1

    var shadowOffset = Vector2f()

    /**
     * The size of the shadow in pixels.
     */
    var shadowSize = 8F

    /**
     * The RGBA color of the shadow.
     */
    var shadowColor = Vector4f()

    val dropShadowShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/sdf/drop_shadow.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(
            UniformProvider("signedDistanceField") { pointer ->
                glActiveTexture(GlConst.GL_TEXTURE0)
                glBindTexture(GlConst.GL_TEXTURE_2D, signedDistanceField)
                glUniform1i(pointer, 0)
            },
            UniformProvider("shadowOffset") { pointer ->
                glUniform2f(pointer, shadowOffset.x, shadowOffset.y)
            },
            UniformProvider("shadowSize") { pointer ->
                glUniform1f(pointer, shadowSize)
            },
            UniformProvider("shadowColor") { pointer ->
                glUniform4f(pointer, shadowColor.x, shadowColor.y, shadowColor.z, shadowColor.w)
            }
        )
    )

    fun render(signedDistanceField: Framebuffer) {
        this.signedDistanceField = signedDistanceField.colorAttachment
        dropShadowShader.enableShader()
        BlitProgram.blit()
        dropShadowShader.disableShader()
        this.signedDistanceField = -1
    }
}