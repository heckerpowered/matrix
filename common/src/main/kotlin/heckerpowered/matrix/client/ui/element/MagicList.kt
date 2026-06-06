/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import net.minecraft.world.entity.LivingEntity
import kotlin.math.roundToInt

object MagicList {
    fun render(
        drawContext: GuiGraphicsExtractor,
        magics: List<Magic>,
        selectedIndex: Int,
        target: LivingEntity?,
        alpha: Double,
    ) {
        if (magics.isEmpty()) {
            return
        }

        val width = drawContext.guiWidth()
        val height = drawContext.guiHeight()
        val panelWidth = 232
        val rowHeight = 20
        val visibleRows = magics.size.coerceAtMost(9)
        val panelHeight = visibleRows * rowHeight + 28
        val left = width - panelWidth - 18
        val top = (height - panelHeight) / 2
        val right = left + panelWidth
        val bottom = top + panelHeight

        HudShaderLayer.applyMagicListBackdrop(drawContext, left, top, right, bottom, alpha)

        val titleAlpha = (alpha * 255).roundToInt()
        drawContext.text(
            minecraft.font,
            Component.translatable("matrix.hud.magic_list"),
            left + 12,
            top + 8,
            color(titleAlpha, 234, 246, 255),
        )

        val firstIndex = firstVisibleIndex(selectedIndex, magics.size, visibleRows)
        val player = minecraft.player

        for (visibleIndex in 0 until visibleRows) {
            val magicIndex = firstIndex + visibleIndex
            val magic = magics.getOrNull(magicIndex) ?: continue
            val rowTop = top + 24 + visibleIndex * rowHeight
            val selected = magicIndex == selectedIndex
            val rowAlpha = (alpha * if (selected) 210 else 96).roundToInt()
            val accent = if (selected) 0x76D8FF else 0x2F6E82

            drawContext.fill(left + 8, rowTop, right - 8, rowTop + rowHeight - 2, color(rowAlpha, 8, 18, 24))
            drawContext.fill(left + 8, rowTop, left + 11, rowTop + rowHeight - 2, color((alpha * 220).roundToInt(), accent shr 16 and 0xFF, accent shr 8 and 0xFF, accent and 0xFF))

            val calculationContext = MagicCalculationContext.fromEntity(player, target)
            val cost = runCatching { magic.getCost(calculationContext) }.getOrDefault(magic.definition.baseCost.toDouble().toLong())
            val channelTime = runCatching { magic.getChannelTime(calculationContext) }.getOrDefault(magic.definition.baseChannelTime.ticks)
            val available = target != null && runCatching { magic.availableStatus(calculationContext).isAvailable }.getOrDefault(false)
            val textAlpha = (alpha * if (available || selected) 255 else 150).roundToInt()

            drawContext.text(minecraft.font, magic.definition.name, left + 16, rowTop + 5, color(textAlpha, 244, 250, 255))

            val costText = "${cost.mana.toDouble().roundToInt()}M"
            val timeText = "${channelTime.coerceAtLeast(0)}t"
            drawContext.text(minecraft.font, costText, right - 72, rowTop + 5, color(textAlpha, 144, 229, 255))
            drawContext.text(minecraft.font, timeText, right - 38, rowTop + 5, color(textAlpha, 188, 255, 218))
        }

        val footer = target?.displayName ?: Component.translatable("matrix.magic.available_status.target_missing")
        drawContext.text(minecraft.font, footer, left + 12, bottom - 13, color((alpha * 170).roundToInt(), 214, 232, 242))
    }

    private fun firstVisibleIndex(selectedIndex: Int, size: Int, visibleRows: Int): Int {
        if (size <= visibleRows) {
            return 0
        }
        return (selectedIndex - visibleRows / 2).coerceIn(0, size - visibleRows)
    }
}

object HudShaderLayer {
    fun applyMagicListBackdrop(
        drawContext: GuiGraphicsExtractor,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        alpha: Double,
    ) {
        val opacity = alpha.coerceIn(.0, 1.0)
        val screenWidth = drawContext.guiWidth()
        val screenHeight = drawContext.guiHeight()

        drawContext.fill(0, 0, screenWidth, screenHeight, color((opacity * 76).roundToInt(), 2, 8, 12))
        drawContext.fill(left, top, right, bottom, color((opacity * 184).roundToInt(), 4, 14, 20))
        drawContext.fill(left + 1, top + 1, right - 1, bottom - 1, color((opacity * 64).roundToInt(), 45, 186, 224))

        var scanY = top + 6
        while (scanY < bottom - 4) {
            drawContext.fill(left + 4, scanY, right - 4, scanY + 1, color((opacity * 28).roundToInt(), 120, 226, 255))
            scanY += 8
        }

        drawContext.fill(left, top, right, top + 1, color((opacity * 230).roundToInt(), 118, 216, 255))
        drawContext.fill(left, bottom - 1, right, bottom, color((opacity * 120).roundToInt(), 118, 216, 255))
        drawContext.fill(left, top, left + 1, bottom, color((opacity * 120).roundToInt(), 118, 216, 255))
        drawContext.fill(right - 1, top, right, bottom, color((opacity * 120).roundToInt(), 118, 216, 255))
    }
}

private fun color(alpha: Int, red: Int, green: Int, blue: Int): Int {
    return ARGB.color(alpha.coerceIn(0, 255), red.coerceIn(0, 255), green.coerceIn(0, 255), blue.coerceIn(0, 255))
}
