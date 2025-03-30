package heckerpowered.matrix.client.ui.element

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.BlurRenderer
import heckerpowered.matrix.client.ui.foundation.animation.ColorAnimation
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.persistent.getChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import java.time.Duration
import kotlin.math.abs
import kotlin.math.min

object ManaCostTooltip {
    private val shownAnimation = SimpleDoubleAnimation()
    private val opacityAnimation = SimpleDoubleAnimation()
    private val backgroundColorAnimation = ColorAnimation()
    private var visibility = false

    private var differenceChangedAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(150))
    private var lastDifference = 0L
    private var displayedDifference = 0L

    fun show() {
        if (!visibility) {
            return
        }
        shownAnimation.value = 70.0
        opacityAnimation.value = 1.0
    }

    fun hide() {
        shownAnimation.value = .0
        opacityAnimation.value = 0.0
    }

    fun onHudVisibilityChanged(visibility: Boolean) {
        this.visibility = visibility
        if (!visibility) {
            hide()
        }
    }

    fun render(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val currentMagic = MatrixHud.selectedMagic
        val target = MatrixHud.targetedEntity
        val channelSequence = player.getChannelSequence(target)
        val cost = currentMagic.getCost(player, target, channelSequence)
        val normalCost = currentMagic.getNormalCost()
        if (cost == normalCost) {
            hide()
        } else {
            show()
        }

        if (cost > normalCost) {
            backgroundColorAnimation.red.value = 127.0
            backgroundColorAnimation.green.value = .0
            backgroundColorAnimation.blue.value = .0
        } else if (cost < normalCost) {
            backgroundColorAnimation.red.value = .0
            backgroundColorAnimation.green.value = 127.0
            backgroundColorAnimation.blue.value = .0
        }

        if (opacityAnimation.animatedValue == .0) {
            return
        }

        val minPoint = Point(drawContext.scaledWindowWidth / 2 - 125.0, drawContext.scaledWindowHeight - shownAnimation.animatedValue)
        val maxPoint = Point(drawContext.scaledWindowWidth / 2 + 125.0, minPoint.y + 15)

        drawContext.enableScissor(
            minPoint.x.toInt(),
            minPoint.y.toInt(),
            maxPoint.x.toInt(),
            maxPoint.y.toInt()
        )
        BlurRenderer.blurTextureRenderShader.enableShader()
        BlitShader.blit()
        //BlurRenderer.renderQuad()
        BlurRenderer.blurTextureRenderShader.disableShader()
        drawContext.disableScissor()

        val color = backgroundColorAnimation
        val red = color.red.animatedValue.toFloat()
        val green = color.green.animatedValue.toFloat()
        val blue = color.blue.animatedValue.toFloat()
        val alpha = (opacityAnimation.animatedValue * 0.5).toFloat()

        val transformationMatrix = drawContext.matrices.peek().positionMatrix
        val tessellator = Tessellator.getInstance()

        val buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
        buffer.vertex(transformationMatrix, maxPoint.x.toFloat(), maxPoint.y.toFloat(), 0.0F).color(red, green, blue, alpha)
        buffer.vertex(transformationMatrix, maxPoint.x.toFloat(), minPoint.y.toFloat(), 0.0F).color(red, green, blue, alpha)
        buffer.vertex(transformationMatrix, minPoint.x.toFloat(), minPoint.y.toFloat(), 0.0F).color(red, green, blue, alpha)
        buffer.vertex(transformationMatrix, minPoint.x.toFloat(), maxPoint.y.toFloat(), 0.0F).color(red, green, blue, alpha)

        RenderSystem.enableBlend()
        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
        BufferRenderer.drawWithGlobalProgram(buffer.end())
        RenderSystem.disableBlend()

        val textRenderer = minecraft.textRenderer
        val difference = abs(normalCost - cost)
        if (difference != lastDifference) {
            differenceChangedAnimation.value = .0
            lastDifference = difference
        }
        if (differenceChangedAnimation.animatedValue == .0) {
            differenceChangedAnimation.value = 1.0
            displayedDifference = difference
        }

        val foregroundOpacity = min(differenceChangedAnimation.animatedValue, opacityAnimation.animatedValue)
        val foregroundColor = Color(255, 255, 255, (opacityAnimation.animatedValue * 255).toInt())
        val differenceForegroundColor = Color(255, 255, 255, (foregroundOpacity * 255).toInt())
        if ((opacityAnimation.animatedValue * 255).toInt() <= 3) {
            return
        }

        val vertexConsumerProvider = drawContext.vertexConsumers
        val yOffset = 2.5F
        if (cost > normalCost) {
            textRenderer.draw(MatrixLanguage.manaCostIncreased, minPoint.x.toFloat() + 5F, minPoint.y.toFloat() + yOffset, foregroundColor.toInt(), false, transformationMatrix, vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
        } else if (cost < normalCost) {
            textRenderer.draw(MatrixLanguage.manaCostReduced, minPoint.x.toFloat() + 5F, minPoint.y.toFloat() + yOffset, foregroundColor.toInt(), false, transformationMatrix, vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
        }

        if ((foregroundOpacity * 255).toInt() > 3 && cost != normalCost) {
            val width = textRenderer.getWidth(displayedDifference.toString())
            textRenderer.draw(displayedDifference.toString(), maxPoint.x.toFloat() - 5F - width, minPoint.y.toFloat() + yOffset, differenceForegroundColor.toInt(), false, transformationMatrix, vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
        }
        drawContext.draw()
    }
}