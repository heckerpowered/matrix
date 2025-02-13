package heckerpowered.matrix.client.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import kotlin.math.max
import kotlin.math.min

class MatrixHUDRenderer(val drawContext: DrawContext, val tickCounter: RenderTickCounter) {
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
        val transformationMatrix = drawContext.matrices.peek().positionMatrix
        val tessellator = Tessellator.getInstance()

        val buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
    }
}

class LegacyMatrixUIRenderer(private val vertexConsumers: VertexConsumerProvider.Immediate) {
    private val minecraft = MinecraftClient.getInstance()
    private val matrixStack = MatrixStack()
    private val textRenderer = minecraft.textRenderer

    val scaledWindowWidth: Int
        get() = minecraft.window.scaledWidth

    val scaledWindowHeight: Int
        get() = minecraft.window.scaledHeight

    private fun renderOnLayer(layer: RenderLayer, rectangle: Rectangle, color: Color) {
        val maxX = max(rectangle.min.x, rectangle.max.x)
        val maxY = max(rectangle.min.y, rectangle.max.y)

        val minX = min(rectangle.min.x, rectangle.max.x)
        val minY = min(rectangle.min.y, rectangle.max.y)

        val vertexConsumer = vertexConsumers.getBuffer(layer)
        val matrix = matrixStack.peek().positionMatrix
        vertexConsumer.vertex(matrix, maxX.toFloat(), maxY.toFloat(), 0f).color(color.toInt())
        vertexConsumer.vertex(matrix, maxX.toFloat(), minY.toFloat(), 0f).color(color.toInt())
        vertexConsumer.vertex(matrix, minX.toFloat(), minY.toFloat(), 0f).color(color.toInt())
        vertexConsumer.vertex(matrix, minX.toFloat(), maxY.toFloat(), 0f).color(color.toInt())

        render()
    }
    
    private fun render() {
        RenderSystem.disableDepthTest()
        vertexConsumers.draw()
        RenderSystem.enableDepthTest()
    }

    fun renderRectangle(rectangle: Rectangle, color: Color) {
        renderOnLayer(RenderLayer.getGui(), rectangle, color)
    }

    fun render(text: Text, point: Point, color: Color, shadow: Boolean = false) {
        textRenderer.draw(
            text,
            point.x.toFloat(),
            point.y.toFloat(),
            color.toInt(),
            shadow,
            matrixStack.peek().positionMatrix,
            vertexConsumers,
            TextRenderer.TextLayerType.NORMAL,
            0,
            0xF000F0
        )
        render()
    }
}