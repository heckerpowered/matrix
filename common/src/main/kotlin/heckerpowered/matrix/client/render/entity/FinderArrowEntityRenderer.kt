/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.entity

import heckerpowered.matrix.client.render.ScreenEffectRenderer
// GPU particle system retired (see common/attic)
// import heckerpowered.matrix.client.render.ScreenEffectRenderer.particleSystem
// import heckerpowered.matrix.client.render.particle.module.particle_spawn.InitializeParticleModule
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
    override fun createRenderState(): ArrowRenderState {
        return ArrowRenderState()
    }

    override fun getTextureLocation(state: ArrowRenderState): Identifier {
        return TEXTURE
    }

    override fun extractRenderState(entity: FinderArrowEntity, state: ArrowRenderState, partialTick: Float) {
        super.extractRenderState(entity, state, partialTick)

        // GPU particle system retired (see common/attic)
        // val particleState = (particleSystem.particleSpawnModules.first { it is InitializeParticleModule } as InitializeParticleModule).particleState
        //
        // particleState.age = 0F
        ScreenEffectRenderer.spawnParticleAt(entity.getLerpedPos(partialTick), 10)
    }

    companion object {
        val TEXTURE: Identifier = Identifier.withDefaultNamespace("textures/entity/projectiles/spectral_arrow.png")
    }
}
