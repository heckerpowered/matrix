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
class EmptyRenderer<T : Entity>(context: EntityRendererProvider.Context) : EntityRenderer<T, EntityRenderState>(context) {
    override fun shouldRender(entity: T, frustum: Frustum, x: Double, y: Double, z: Double): Boolean {
        return false
    }

    override fun createRenderState(): EntityRenderState {
        return EntityRenderState()
    }
}
