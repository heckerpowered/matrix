/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.core.description
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import net.minecraft.world.entity.LivingEntity
import kotlin.math.roundToInt

object MagicList {
    private const val MAGIC_ELEMENT_HEIGHT = 100
    private const val MAGIC_ELEMENT_MARGIN = 50
    private const val MAGIC_ELEMENT_SPAN = 10

    fun render(
        drawContext: GuiGraphicsExtractor,
        magics: List<Magic>,
        selectedIndex: Int,
        target: LivingEntity?,
        alpha: Double,
        availableMana: Double,
        maxMana: Double,
        manaRate: Double,
        magicRate: Double,
    ) {
        if (magics.isEmpty()) {
            return
        }

        val width = drawContext.guiWidth()
        val height = drawContext.guiHeight()

        HudShaderLayer.applyMagicListBackdrop(drawContext, alpha)

        val visibleRows = ((height - 70) / (MAGIC_ELEMENT_HEIGHT + MAGIC_ELEMENT_MARGIN))
            .coerceAtLeast(1)
            .coerceAtMost(magics.size)
        val firstIndex = firstVisibleIndex(selectedIndex, magics.size, visibleRows)
        val indentList = generateIndentList(visibleRows, MAGIC_ELEMENT_SPAN)
        val baseTop = height / 2 - (visibleRows * (MAGIC_ELEMENT_HEIGHT + MAGIC_ELEMENT_MARGIN) - MAGIC_ELEMENT_MARGIN) / 2
        val player = minecraft.player

        for (visibleIndex in 0 until visibleRows) {
            val magicIndex = firstIndex + visibleIndex
            val magic = magics.getOrNull(magicIndex) ?: continue
            val indent = indentList.getOrElse(visibleIndex) { 0 }
            val rowTop = baseTop + visibleIndex * (MAGIC_ELEMENT_HEIGHT + MAGIC_ELEMENT_MARGIN)
            val rowLeft = 50 + indent
            val rowWidth = 270.coerceAtMost((width * 0.42).roundToInt())
            val selected = magicIndex == selectedIndex
            val rowAlpha = (alpha * if (selected) 190 else 72).roundToInt()
            val accent = if (selected) 0x76D8FF else 0x2F6E82

            drawContext.fill(rowLeft, rowTop, rowLeft + rowWidth, rowTop + MAGIC_ELEMENT_HEIGHT, color(rowAlpha, 7, 16, 22))
            drawContext.outline(rowLeft, rowTop, rowWidth, MAGIC_ELEMENT_HEIGHT, color((alpha * if (selected) 185 else 80).roundToInt(), 118, 216, 255))
            drawContext.fill(rowLeft, rowTop, rowLeft + 5, rowTop + MAGIC_ELEMENT_HEIGHT, color((alpha * 220).roundToInt(), accent shr 16 and 0xFF, accent shr 8 and 0xFF, accent and 0xFF))

            val calculationContext = MagicCalculationContext.fromEntity(player, target)
            val cost = runCatching { magic.getCost(calculationContext) }.getOrDefault(magic.definition.baseCost.toDouble().toLong())
            val channelTime = runCatching { magic.getChannelTime(calculationContext) }.getOrDefault(magic.definition.baseChannelTime.ticks)
            val availability = runCatching { magic.availableStatus(calculationContext) }.getOrNull()
            val available = availability?.isAvailable == true
            val textAlpha = (alpha * if (available || selected) 255 else 150).roundToInt()

            drawContext.text(minecraft.font, magic.definition.name, rowLeft + 14, rowTop + 12, color(textAlpha, 244, 250, 255))

            val costText = "${cost.mana.toDouble().roundToInt()}M"
            val timeText = "${channelTime.coerceAtLeast(0)}t"
            drawContext.text(minecraft.font, costText, rowLeft + 14, rowTop + 33, color(textAlpha, 144, 229, 255))
            drawContext.text(minecraft.font, timeText, rowLeft + 74, rowTop + 33, color(textAlpha, 188, 255, 218))

            val availabilityText = availability?.firstOrNull()?.description ?: MatrixLanguage.magicAvailable
            drawContext.textWithWordWrap(
                minecraft.font,
                availabilityText,
                rowLeft + 14,
                rowTop + 54,
                rowWidth - 28,
                if (available) color((alpha * 180).roundToInt(), 188, 255, 218) else color((alpha * 190).roundToInt(), 255, 192, 128),
            )
        }

        val detailRight = width - 42
        val detailLeft = (width * 0.58).roundToInt().coerceAtMost(detailRight - 260)
        val detailTop = 58
        val detailBottom = height - 40
        drawPanel(drawContext, detailLeft, detailTop, detailRight, detailBottom, alpha)
        drawContext.text(
            minecraft.font,
            "Mana ${availableMana.toInt()}/${maxMana.toInt()}   M x${formatRate(manaRate)}   C x${formatRate(magicRate)}",
            detailLeft + 12,
            detailTop - 18,
            color((alpha * 190).roundToInt(), 178, 226, 246),
        )
        renderDetails(drawContext, detailLeft, detailTop, detailRight, detailBottom, magics[selectedIndex.coerceIn(magics.indices)], target, alpha)
    }

    private fun renderDetails(
        drawContext: GuiGraphicsExtractor,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        magic: Magic,
        target: LivingEntity?,
        alpha: Double,
    ) {
        val contentLeft = left + 12
        val contentWidth = right - left - 24
        var y = top + 12
        val textColor = color((alpha * 245).roundToInt(), 234, 246, 255)
        val mutedColor = color((alpha * 175).roundToInt(), 160, 201, 222)

        drawContext.text(minecraft.font, Component.translatable("matrix.hud.target"), contentLeft, y, mutedColor)
        y += 13
        if (target != null) {
            drawContext.text(minecraft.font, target.displayName, contentLeft, y, textColor)
            y += 13
            val healthText = "HP ${target.health.roundToInt()}/${target.maxHealth.roundToInt()}"
            drawContext.text(minecraft.font, healthText, contentLeft, y, color((alpha * 220).roundToInt(), 188, 255, 218))
            y += 22
        } else {
            drawContext.text(minecraft.font, Component.translatable("matrix.magic.available_status.target_missing"), contentLeft, y, color((alpha * 210).roundToInt(), 255, 170, 150))
            y += 35
        }

        drawContext.text(minecraft.font, Component.translatable("matrix.hud.selected_magic"), contentLeft, y, mutedColor)
        y += 13
        drawContext.text(minecraft.font, magic.definition.name, contentLeft, y, textColor)
        y += 15

        val calculationContext = MagicCalculationContext.fromEntity(minecraft.player, target)
        val cost = runCatching { magic.getCost(calculationContext) }.getOrDefault(magic.definition.baseCost.toDouble().toLong())
        val channelTime = runCatching { magic.getChannelTime(calculationContext) }.getOrDefault(magic.definition.baseChannelTime.ticks)
        drawContext.text(minecraft.font, "${cost.mana.toDouble().roundToInt()}M   ${channelTime.coerceAtLeast(0)}t", contentLeft, y, color((alpha * 220).roundToInt(), 144, 229, 255))
        y += 18

        val availability = runCatching { magic.availableStatus(calculationContext) }.getOrNull()
        val availabilityText = availability?.firstOrNull()?.description ?: MatrixLanguage.magicAvailable
        drawContext.text(minecraft.font, availabilityText, contentLeft, y, if (availability?.isAvailable == true) color((alpha * 220).roundToInt(), 188, 255, 218) else color((alpha * 220).roundToInt(), 255, 192, 128))
        y += 20

        drawContext.textWithWordWrap(minecraft.font, magic.definition.description, contentLeft, y, contentWidth, color((alpha * 205).roundToInt(), 214, 232, 242))

        drawContext.horizontalLine(contentLeft, right - 12, bottom - 18, color((alpha * 80).roundToInt(), 118, 216, 255))
    }

    private fun firstVisibleIndex(selectedIndex: Int, size: Int, visibleRows: Int): Int {
        if (size <= visibleRows) {
            return 0
        }
        return (selectedIndex - visibleRows / 2).coerceIn(0, size - visibleRows)
    }

    private fun generateIndentList(size: Int, indentSize: Int): List<Int> {
        if (size <= 1) {
            return listOf(0)
        }

        val center = (size - 1) / 2.0
        return List(size) { index ->
            val distance = kotlin.math.abs(index - center)
            ((center - distance) * indentSize).roundToInt().coerceAtLeast(0)
        }
    }

    private fun drawPanel(drawContext: GuiGraphicsExtractor, left: Int, top: Int, right: Int, bottom: Int, alpha: Double) {
        drawContext.fill(left, top, right, bottom, color((alpha * 150).roundToInt(), 4, 14, 20))
        drawContext.fill(left + 1, top + 1, right - 1, bottom - 1, color((alpha * 42).roundToInt(), 45, 186, 224))
        drawContext.outline(left, top, right - left, bottom - top, color((alpha * 150).roundToInt(), 118, 216, 255))
    }

    private fun formatRate(value: Double): String {
        return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
    }
}

object HudShaderLayer {
    fun applyMagicListBackdrop(
        drawContext: GuiGraphicsExtractor,
        alpha: Double,
    ) {
        val opacity = alpha.coerceIn(.0, 1.0)
        val screenWidth = drawContext.guiWidth()
        val screenHeight = drawContext.guiHeight()

        drawContext.fill(0, 0, screenWidth, screenHeight, color((opacity * 88).roundToInt(), 2, 8, 12))
        drawContext.fillGradient(0, 0, screenWidth, screenHeight, color((opacity * 52).roundToInt(), 17, 45, 55), color((opacity * 18).roundToInt(), 2, 8, 12))

        var scanY = 30
        while (scanY < screenHeight) {
            drawContext.fill(0, scanY, screenWidth, scanY + 1, color((opacity * 18).roundToInt(), 120, 226, 255))
            scanY += 8
        }
    }
}

private fun color(alpha: Int, red: Int, green: Int, blue: Int): Int {
    return ARGB.color(alpha.coerceIn(0, 255), red.coerceIn(0, 255), green.coerceIn(0, 255), blue.coerceIn(0, 255))
}
