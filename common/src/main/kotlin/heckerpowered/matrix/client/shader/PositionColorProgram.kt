/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import heckerpowered.matrix.client.render.Color

/**
 * Vertex-color modulation shader (`position_color.vsh`/`.fsh`): multiplies each vertex's own
 * color attribute by [color] uniformly.
 *
 * 26.2 note: this was a mesh-attached [Program], bound globally via `enableShader()` around
 * vanilla `BufferBuilder` draw calls with `Position`/`Color` vertex attributes -- there is no
 * wrapper-API equivalent for "bind this program globally, then let unrelated immediate-mode
 * code draw against it" (render passes are self-contained and target an explicit
 * [com.mojang.blaze3d.pipeline.RenderTarget] with a [com.mojang.blaze3d.pipeline.RenderPipeline]
 * bound per draw call, not a globally bound program). `position_color.vsh`/`.fsh` also have no
 * std140-converted `post/` sibling (unlike the dissolve shaders) since they were never a
 * fullscreen pass to begin with. No caller currently references this object in the tree.
 * [color] is kept as plain state for whoever ports the mesh-draw call site onto a
 * [com.mojang.blaze3d.pipeline.RenderPipeline].
 */
object PositionColorProgram {
    var color = Color(1, 1, 1, 1)
}
