/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.entity

import heckerpowered.matrix.common.entity.DevEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState

@Environment(EnvType.CLIENT)
class DevEntityRenderer(context: EntityRendererProvider.Context) : EntityRenderer<DevEntity, EntityRenderState>(context) {
    override fun createRenderState(): EntityRenderState {
        return EntityRenderState()
    }
}
