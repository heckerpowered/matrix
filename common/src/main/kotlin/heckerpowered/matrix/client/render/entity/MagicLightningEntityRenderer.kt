/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.entity

import heckerpowered.matrix.common.entity.MagicLightningBolt
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.LightningBoltRenderState

@Environment(EnvType.CLIENT)
class MagicLightningEntityRenderer(context: EntityRendererProvider.Context) : EntityRenderer<MagicLightningBolt, LightningBoltRenderState>(context) {
    override fun createRenderState(): LightningBoltRenderState {
        return LightningBoltRenderState()
    }

    override fun extractRenderState(entity: MagicLightningBolt, state: LightningBoltRenderState, tickDelta: Float) {
        super.extractRenderState(entity, state, tickDelta)
        state.seed = entity.seed
    }

    override fun affectedByCulling(entity: MagicLightningBolt): Boolean {
        return false
    }
}
