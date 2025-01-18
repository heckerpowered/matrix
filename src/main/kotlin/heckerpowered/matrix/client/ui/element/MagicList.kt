package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.Magic
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats

/**
 * Renderer of the magic list in the HUD.
 */
object MagicList {

    const val MAGIC_ELEMENT_HEIGHT = 100.0
    const val MAGIC_ELEMENT_SPAN = 5.0

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
    private fun updateUseAnimationList(tickCounter: RenderTickCounter) {
        for (i in animationList.indices) {
            val animation = animationList[i]
            if (animation == .0) {
                continue
            }

            // Just update the animation for the current magic, they will automatically stop
            // when they reach their full duration.
            animationList[i] = animation + tickCounter.lastFrameDuration / 2000000
        }
    }

    fun render(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        if (magics.isEmpty()) {
            return
        }

        updateUseAnimationList(tickCounter)
    }

    private fun renderMagic(index: Int, drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val magic = magics[index]
        val indent = indentList[index]

        val transformationMatrix = drawContext.matrices.peek().positionMatrix
        val tessellator = Tessellator.getInstance()

        val buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

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