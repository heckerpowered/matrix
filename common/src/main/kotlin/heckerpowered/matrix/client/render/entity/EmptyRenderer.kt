/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.entity

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.world.entity.Entity

@Environment(EnvType.CLIENT)
class EmptyRenderer(context: EntityRendererProvider.Context) : EntityRenderer<Entity, EntityRenderState>(context) {
    override fun shouldRender(entity: Entity, frustum: Frustum, x: Double, y: Double, z: Double): Boolean {
        return false
    }

    override fun createRenderState(): EntityRenderState = EntityRenderState()
}
