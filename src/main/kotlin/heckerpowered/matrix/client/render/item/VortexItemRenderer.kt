/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.item

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack

object VortexItemRenderer : DynamicItemRenderer {
    override fun render(itemStack: ItemStack, modelTransformationMode: ModelTransformationMode, matrixStack: MatrixStack, vertexConsumerProvider: VertexConsumerProvider, light: Int, overlay: Int) {
        // matrixStack.push()
//
        // val model = minecraft.itemRenderer.getModel(ItemStack(Items.ITEM_FRAME), minecraft.world, minecraft.player, 0)
//
        // // matrixStack.scale(0.5f, 0.5f, 0.5f)
        // // model.transformation.getTransformation(modelTransformationMode).apply(false, matrixStack)
        // // matrixStack.translate(0.5f, 0.5f, 0.5f)
//
        // val matrices = MatrixStack()
        // val matrix = matrices.peek().positionMatrix
//
        // val builder = Tessellator.getInstance()
        // val buffer = builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
//
        // buffer.vertex(matrix, -1F, -1F, 0F).texture(0F, 0F)
        // buffer.vertex(matrix, 1F, -1F, 0F).texture(1F, 0F)
        // buffer.vertex(matrix, 1F, 1F, 0F).texture(1F, 1F)
        // buffer.vertex(matrix, -1F, 1F, 0F).texture(0F, 1F)
//
        // buffer.vertex(matrix, -1F, 1F, 0F).texture(0F, 1F)
        // buffer.vertex(matrix, 1F, 1F, 0F).texture(1F, 1F)
        // buffer.vertex(matrix, 1F, -1F, 0F).texture(1F, 0F)
        // buffer.vertex(matrix, -1F, -1F, 0F).texture(0F, 0F)
//
        // RenderSystem.disableBlend()
        // RenderSystem.disableDepthTest()
        // VortexRenderer.vortexShader.enableShader()
        // BufferRenderer.draw(buffer.end())
        // VortexRenderer.vortexShader.disableShader()
//
        // matrixStack.pop()
    }
}