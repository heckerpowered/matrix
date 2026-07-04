/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.MatrixHud.targetedEntity
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.player
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlurRenderer
import heckerpowered.matrix.client.ui.foundation.animation.ColorAnimation
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicAvailability
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB

/**
 * Renderer of the magic list in the HUD.
 */
object MagicList {

    private const val MAGIC_ELEMENT_HEIGHT = 100.0
    private const val MAGIC_ELEMENT_MARGIN = 50.0
    private const val MAGIC_ELEMENT_SPAN = 10.0

    /**
     * The list of magics to be displayed in the HUD.
     */
    val magics = mutableListOf<Magic>()

    /**
     * Cached indent list for the magics, regenerates when the size of the list changes.
     */
    private var cachedIndentList: List<Double>? = null

    /**
     * Indent list for the magics, regenerates when the size of the list changes.
     */
    private val indentList: List<Double>
        get() {
            if (cachedIndentList?.size != magics.size) {
                cachedIndentList = generateIndentList(magics.size)
            }

            return cachedIndentList!!
        }

    /**
     * Cached use animation list for the magics, regenerates when the size of the list changes.
     */
    private var cachedAnimationList: MutableList<Double>? = null

    /**
     * Cached use animation list for the magics, regenerates when the size of the list changes.
     */
    private val animationList: MutableList<Double>
        get() {
            if (cachedAnimationList?.size != magics.size) {
                cachedAnimationList = mutableListOf<Double>().apply {
                    repeat(magics.size) {
                        add(.0)
                    }
                }
            }

            return cachedAnimationList!!
        }

    /**
     * Extra width animation list for the magics, regenerates when the size of the list changes.
     */
    private var cachedWidthAnimationList: MutableList<SimpleDoubleAnimation>? = null

    /**
     * Width animation list for the magics.
     */
    private val widthAnimationList: MutableList<SimpleDoubleAnimation>
        get() {
            if (cachedWidthAnimationList?.size != magics.size) {
                cachedWidthAnimationList = mutableListOf<SimpleDoubleAnimation>().apply {
                    repeat(magics.size) {
                        add(SimpleDoubleAnimation())
                    }
                }
            }

            return cachedWidthAnimationList!!
        }

    private var cachedBackgroundColorAnimationList: MutableList<ColorAnimation>? = null
    private val backgroundColorAnimationList: MutableList<ColorAnimation>
        get() {
            if (cachedBackgroundColorAnimationList?.size != magics.size) {
                cachedBackgroundColorAnimationList = mutableListOf<ColorAnimation>().apply {
                    repeat(magics.size) {
                        add(ColorAnimation())
                    }
                }
            }

            return cachedBackgroundColorAnimationList!!
        }

    /**
     * Whether the HUD is visible or not.
     */
    private var visibility: Boolean = false

    /**
     * The opacity of the HUD, used for fading in and out.
     */
    private val opacity = SimpleDoubleAnimation()

    /**
     * The x offset animation of the HUD, used for performing a sliding animation.
     */
    private val xOffset = SimpleDoubleAnimation(-50.0)

    /**
     * Update the use animation list for the magics, called every frame.
     */
    private fun updateUseAnimationList(tickCounter: DeltaTracker) {
        for (i in animationList.indices) {
            val animation = animationList[i]
            if (animation == .0) {
                continue
            }

            // Just update the animation for the current magic, they will automatically stop
            // when they reach their full duration.
            // 26.2: old Timer.lastFrameDuration (tick delta of the last rendered frame) is now
            // DeltaTracker.getGameTimeDeltaTicks() (unpaused per-frame tick delta).
            animationList[i] = animation + tickCounter.gameTimeDeltaTicks / 2000000
        }
    }

    fun render(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        if (magics.isEmpty()) {
            return
        }

        updateUseAnimationList(tickCounter)
    }

    private fun getMagicAvailableStatus(magic: Magic): MagicAvailability {
        val calculationContext = MagicCalculationContext.fromEntity(player, targetedEntity)
        return magic.availableStatus(calculationContext)
    }

    private fun calculateMagicWidth(magic: Magic): Double {
        val textRenderer = minecraft.font

        val calculationContext = MagicCalculationContext.fromEntity(player, targetedEntity)
        val costString = magic.getCost(calculationContext).toString()
        val statusString = (magic.availableStatus(calculationContext).firstOrNull()?.description
            ?: MatrixLanguage.magicAvailable).string

        val magicNameWidth = textRenderer.width(magic.definition.name)
        val costWidth = textRenderer.width(costString)
        val statusWidth = textRenderer.width(statusString)

        return magicNameWidth + costWidth + statusWidth + MAGIC_ELEMENT_SPAN * 3 // 3 spans for the name, cost, and status
    }

    private fun renderMagic(index: Int, drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        if (opacity.animatedValue == .0) {
            return
        }

        val magic = magics[index]
        val indent = indentList[index]

        val calculationContext = MagicCalculationContext.fromEntity(player, targetedEntity)
        val costString = magic.getCost(calculationContext).toString()
        val statusString = (magic.availableStatus(calculationContext).firstOrNull()?.description
            ?: MatrixLanguage.magicAvailable).string

        val textRenderer = minecraft.font
        val magicNameWidth = textRenderer.width(magic.definition.name)
        val costWidth = textRenderer.width(costString)
        val statusWidth = textRenderer.width(statusString)

        val magicWidth = magicNameWidth + costWidth + statusWidth + MAGIC_ELEMENT_SPAN * 3 // 3 spans for the name, cost, and status

        val widthAnimation = widthAnimationList[index]
        widthAnimation.value = magicWidth

        val startX = (MAGIC_ELEMENT_MARGIN + indent + xOffset.animatedValue).toFloat()
        val endX = (startX + widthAnimation.animatedValue).toFloat()
        val startY = (index * (MAGIC_ELEMENT_HEIGHT + MAGIC_ELEMENT_MARGIN) + drawContext.guiHeight() / 2 - (indentList.size + 1) * (MAGIC_ELEMENT_HEIGHT + MAGIC_ELEMENT_MARGIN) / 2).toFloat()
        val endY = startY + MAGIC_ELEMENT_HEIGHT.toFloat()

        drawContext.enableScissor(startX.toInt(), startY.toInt(), endX.toInt(), endY.toInt())

        // 1.21 composited the blurred HUD by drawing a scissored fullscreen quad through
        // blur_mask.fsh, whose alpha<0.1 discard produced a hard cutout: faint-alpha fringes
        // never touched the destination. The blit below must stay on the extracted-GUI path
        // (drawContext) for GUI ordering and scissoring, and that path only alpha-blends, so
        // the mask is pre-punched instead: blur_mask.fsh copies blurFramebuffer into a
        // transparent-cleared ping target (blend off, so every discarded texel remains fully
        // transparent), and the blit samples the punched copy — restoring the 1.21 hard-cutout
        // look through ordinary alpha blending.
        PostProcessRenderer.clear(PostProcessRenderer.ping)
        BlurRenderer.renderQuad(PostProcessRenderer.ping)
        PostProcessRenderer.ping.colorTextureView?.let { punchedView ->
            val guiWidth = drawContext.guiWidth().toFloat()
            val guiHeight = drawContext.guiHeight().toFloat()
            drawContext.blit(
                punchedView,
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR),
                startX.toInt(), startY.toInt(),
                (endX - startX).toInt(), (endY - startY).toInt(),
                startX / guiWidth, endX / guiWidth,
                startY / guiHeight, endY / guiHeight
            )
        }

        val backgroundColorAnimation = backgroundColorAnimationList[index]
        val backgroundColor = ARGB.color(
            (opacity.animatedValue * 127.5).toInt(),
            backgroundColorAnimation.red.animatedValue.toInt(),
            backgroundColorAnimation.green.animatedValue.toInt(),
            backgroundColorAnimation.blue.animatedValue.toInt()
        )

        // 26.2: manual Tessellator/BufferRenderer quad replaced by GuiGraphicsExtractor.fill,
        // which draws the same axis-aligned rectangle through the GUI render pipeline.
        drawContext.fill(startX.toInt(), startY.toInt(), endX.toInt(), endY.toInt(), backgroundColor)

        val foregroundColor = ARGB.color((opacity.animatedValue * 255).toInt(), 255, 255, 255)
        drawContext.text(textRenderer, magic.definition.name, startX.toInt() + 5, startY.toInt() + 5, foregroundColor, false)
        drawContext.text(textRenderer, Component.literal(costString), startX.toInt() + magicNameWidth + 15, startY.toInt() + 5, foregroundColor, false)
        drawContext.text(textRenderer, statusString, startX.toInt() + magicNameWidth + costWidth + 25, startY.toInt() + 5, foregroundColor, false)

        drawContext.disableScissor()
    }

    fun onHudVisibilityChanged(visibility: Boolean) {
        if (visibility) {
            opacity.value = 1.0
            xOffset.value = .0
        } else {
            opacity.value = .0
            xOffset.value = -50.0
        }
    }

    /**
     * Generate an indented list with the given indent and size in the approximate shape of a right-facing arc.
     *
     * @param size the size of the list.
     * @param indentSize the size of indent.
     * @return the indented list.
     */
    private fun generateIndentList(size: Int, indentSize: Double = 5.0): List<Double> {
        val indentList = mutableListOf<Double>()

        var current = .0
        for (i in 0 until size) {
            indentList.add(current)

            if (size % 2 == 0 && i == size / 2 - 1) {
                continue
            }

            if (i >= size / 2) {
                current += indentSize
            } else {
                current -= indentSize
            }
        }

        return indentList
    }
}