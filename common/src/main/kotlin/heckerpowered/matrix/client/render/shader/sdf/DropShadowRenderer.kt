/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader.sdf

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider
import org.joml.Vector2f
import org.joml.Vector4f

object DropShadowRenderer {
    /**
     * The color attachment of the signed distance field.
     */
    var signedDistanceField: GpuTextureView? = null

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
        "post/sdf/drop_shadow.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                // MatrixPostData0.xy = shadowOffset, MatrixPostData0.z = shadowSize
                putVec4(shadowOffset.x, shadowOffset.y, shadowSize, 0F)
                // MatrixPostData1 = shadowColor
                putVec4(shadowColor.x, shadowColor.y, shadowColor.z, shadowColor.w)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(
            TextureProvider("signedDistanceField") { signedDistanceField }
        )
    )

    /**
     * Renders the drop shadow, sampling [signedDistanceField], into [target].
     *
     * 26.2: the old implementation drew via a global-state `BlitProgram.blit()` call that
     * submitted into whichever FBO happened to be bound at the call site; that pattern no
     * longer exists under the wrapper API, and there were no existing callers of this method
     * to infer an implicit target from (grepped `DropShadowRenderer.render(` across
     * common/src/main/kotlin — no hits). [target] was added as an explicit required parameter
     * so callers state their destination framebuffer directly, matching how every other
     * renderer in this port (e.g. [heckerpowered.matrix.client.render.PostProcessRenderer])
     * is driven via `PostProcessRenderer.renderShaderToFramebuffer(shader, target)`.
     */
    fun render(signedDistanceField: RenderTarget, target: RenderTarget) {
        this.signedDistanceField = signedDistanceField.colorTextureView
        dropShadowShader.drawTo(target)
        this.signedDistanceField = null
    }
}
