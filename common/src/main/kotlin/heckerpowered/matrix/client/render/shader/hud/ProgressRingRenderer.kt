/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader.hud

import heckerpowered.matrix.client.shader.*
import org.joml.Vector2f
import org.joml.Vector4f

object ProgressRingRenderer {
    /**
     * The progress of the progress ring, between 0.0F and 1.0F, inclusive.
     */
    var progress: Float = 1.0F

    /**
     * The radius of the progress ring in normalized device coordinates (NDC).
     *
     * 0.5 radius = full screen
     */
    var radius: Float = 0.5F

    /**
     * The thickness of the progress ring in normalized device coordinates (NDC).
     * From the edge of the ring to the center of the ring.
     */
    var thickness: Float = 0.1F

    /**
     * The center of the progress ring in normalized device coordinates (NDC).
     */
    var center: Vector2f = Vector2f(0.5F, 0.5F)

    /**
     * The color of the progress ring.
     */
    var color: Vector4f = Vector4f(1.0F)

    /**
     * The aspect ratio (width / height) of the target the ring is drawn into; the fragment
     * shader uses it to keep the ring circular in the anisotropic fullscreen texcoord space.
     */
    var aspectRatio: Float = 1.0F

    // 26.2: the ring was a positioned POSITION_TEXTURE quad in 1.21; it is a fullscreen pass
    // now, with placement carried by the center/radius/thickness uniforms (height-normalized,
    // aspect-corrected in the fragment shader).
    val progressRingShader = BlitProgram(
        "post/hud/progress_ring.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                putVec4(progress, radius, thickness, aspectRatio)
                putVec4(center.x, center.y, 0F, 0F)
                putVec4(color.x, color.y, color.z, color.w)
                putVec4(0F, 0F, 0F, 0F)
            }
        )
    )
}
