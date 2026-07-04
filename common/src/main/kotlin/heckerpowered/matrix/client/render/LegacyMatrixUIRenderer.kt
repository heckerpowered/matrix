/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// 26.2 note: this file's `MatrixHUDRenderer` companion class is dropped — it was unreferenced
// anywhere else in the codebase (only its own definition), and its only real method
// (renderRectangle) never issued a draw call even in the pre-port version (it built a
// BufferBuilder and discarded it without calling BufferRenderer.draw), so it was already dead
// code before this port. `enableScissor`/`disableScissor` are one-line GuiGraphicsExtractor
// passthroughs with no logic of their own to preserve; if a future caller needs them they can
// be called on GuiGraphicsExtractor directly (`drawContext.enableScissor(...)`).
//
// `LegacyMatrixUIRenderer` itself IS referenced (ManaBar.kt, AvailableStatusTooltip.kt,
// MatrixHud.kt — all out of scope for this render-port pass) for `renderRectangle`,
// `render` (text), `scaledWindowWidth`/`scaledWindowHeight`. Its old
// `VertexConsumerProvider.Immediate`-based implementation (build a quad into a buffer, then
// `vertexConsumers.draw()`) has no equivalent: immediate-mode VertexConsumerProvider is gone in
// 26.2. It is rewritten to draw directly via `GuiGraphicsExtractor.fill()`/`text()` (which
// SystemCrashBar.kt's own port already established as the replacement pattern for this exact
// situation — see the comment above `SystemCrashBar.renderRectangle`), preserving the same
// rectangles/text/colors.
//
// The constructor changes from `LegacyMatrixUIRenderer(vertexConsumers)` to
// `LegacyMatrixUIRenderer(drawContext)` since there is no standalone VertexConsumerProvider to
// hold onto anymore — every draw needs the GuiGraphicsExtractor. Call sites in MatrixHud.kt
// (`LegacyMatrixUIRenderer(drawContext.vertexConsumers)`, 3 occurrences) need to change to
// `LegacyMatrixUIRenderer(drawContext)` to match; left as-is here since MatrixHud.kt is out of
// scope for this pass.
@Deprecated("Deprecated")
class LegacyMatrixUIRenderer(private val drawContext: GuiGraphicsExtractor) {
    private val minecraft = Minecraft.getInstance()
    private val textRenderer = minecraft.font

    val scaledWindowWidth: Int
        get() = drawContext.guiWidth()

    val scaledWindowHeight: Int
        get() = drawContext.guiHeight()

    fun renderRectangle(rectangle: Rectangle, color: Color) {
        val maxX = max(rectangle.min.x, rectangle.max.x).roundToInt()
        val maxY = max(rectangle.min.y, rectangle.max.y).roundToInt()
        val minX = min(rectangle.min.x, rectangle.max.x).roundToInt()
        val minY = min(rectangle.min.y, rectangle.max.y).roundToInt()

        drawContext.fill(minX, minY, maxX, maxY, color.toInt())
    }

    fun render(text: Component, point: Point, color: Color, shadow: Boolean = false) {
        drawContext.text(textRenderer, text, point.x.roundToInt(), point.y.roundToInt(), color.toInt(), shadow)
    }
}