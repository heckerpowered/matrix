/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

@Deprecated("Deprecated")
class MatrixHUDRenderer(val drawContext: GuiGraphicsExtractor, val tickCounter: DeltaTracker) {
    fun enableScissor(rectangle: Rectangle) {
        drawContext.enableScissor(
            rectangle.min.x.toInt(),
            rectangle.min.y.toInt(),
            rectangle.max.x.toInt(),
            rectangle.max.y.toInt()
        )
    }

    fun disableScissor() {
        drawContext.disableScissor()
    }

    fun renderRectangle(rectangle: Rectangle, color: Color) {
        drawContext.fill(
            rectangle.min.x.toInt(),
            rectangle.min.y.toInt(),
            rectangle.max.x.toInt(),
            rectangle.max.y.toInt(),
            color.toInt()
        )
    }
}

@Deprecated("Deprecated")
class LegacyMatrixUIRenderer(private val vertexConsumers: Any? = null) {
    val scaledWindowWidth: Int get() = 0
    val scaledWindowHeight: Int get() = 0

    fun renderRectangle(rectangle: Rectangle, color: Color) = Unit
    fun render(text: Component, point: Point, color: Color, shadow: Boolean = false) = Unit
}
