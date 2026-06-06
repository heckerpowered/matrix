/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.entity

import heckerpowered.matrix.client.render.ScreenEffectRenderer
import heckerpowered.matrix.common.entity.FinderArrowEntity
import heckerpowered.matrix.core.getLerpedPos
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.entity.ArrowRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.ArrowRenderState
import net.minecraft.resources.Identifier

@Environment(EnvType.CLIENT)
class FinderArrowEntityRenderer(context: EntityRendererProvider.Context) : ArrowRenderer<FinderArrowEntity, ArrowRenderState>(context) {
    override fun getTextureLocation(state: ArrowRenderState): Identifier {
        return TEXTURE
    }

    override fun createRenderState(): ArrowRenderState {
        return ArrowRenderState()
    }

    override fun extractRenderState(entity: FinderArrowEntity, state: ArrowRenderState, tickDelta: Float) {
        super.extractRenderState(entity, state, tickDelta)
        ScreenEffectRenderer.spawnParticleAt(entity.getLerpedPos(tickDelta), 10)
    }

    companion object {
        val TEXTURE: Identifier = Identifier.withDefaultNamespace("textures/entity/projectiles/spectral_arrow.png")
    }
}
