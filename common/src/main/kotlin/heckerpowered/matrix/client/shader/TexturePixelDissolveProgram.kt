/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

object TexturePixelDissolveProgram : BlitProgram() {
    var noiseTextureId: Int = 0
    var normalTextureId: Int = 0
    var dissolveFactor: Float = 0.0F
}
