/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.entity

import heckerpowered.matrix.common.entity.DevEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class DevEntityRenderer(context: EntityRendererFactory.Context) : MobEntityRenderer<DevEntity, PlayerEntityModel<DevEntity>>(
    context, PlayerEntityModel(context.getPart(EntityModelLayers.PLAYER), false), 0.5F
) {
    override fun getTexture(entity: DevEntity): Identifier {
        return DefaultSkinHelper.getTexture()
    }
}