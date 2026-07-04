/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.entity

import heckerpowered.matrix.common.entity.DevEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.model.player.PlayerModel
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.resources.Identifier

/**
 * [DevEntity] is a [net.minecraft.world.entity.PathfinderMob], not a player, but it renders
 * with the default player skin/model (1.21 behavior: `PlayerModel<DevEntity>` +
 * `DefaultPlayerSkin.getTexture()`).
 *
 * 26.2 note: [PlayerModel] is no longer generic — it hard-codes its render-state type to
 * [AvatarRenderState] (`class PlayerModel : HumanoidModel<AvatarRenderState>`), and
 * [AvatarRenderState] extraction is normally only done by the player-specific renderer from an
 * `AbstractClientPlayer`. Since [MobRenderer] only requires `S : LivingEntityRenderState`
 * (which [AvatarRenderState] satisfies), we use [AvatarRenderState] directly as this renderer's
 * state and populate it ourselves in [extractRenderState]: base living-entity fields come from
 * `super.extractRenderState`, arm pose / held-item fields come from the same
 * [ArmedEntityRenderState.extractArmedEntityRenderState] helper the vanilla humanoid renderers
 * use (entity-agnostic), and the player-only fields (skin, cape/sleeve/pants visibility) are
 * filled with fixed "plain default skin, fully clothed, no cape" values since [DevEntity] has
 * no real player profile to derive them from.
 */
@Environment(EnvType.CLIENT)
class DevEntityRenderer(context: EntityRendererProvider.Context) : MobRenderer<DevEntity, AvatarRenderState, PlayerModel>(
    context, PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F
) {
    override fun createRenderState(): AvatarRenderState = AvatarRenderState()

    override fun extractRenderState(entity: DevEntity, state: AvatarRenderState, partialTick: Float) {
        super.extractRenderState(entity, state, partialTick)
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, itemModelResolver, partialTick)

        state.skin = DefaultPlayerSkin.getDefaultSkin()
        state.showHat = true
        state.showJacket = true
        state.showLeftPants = true
        state.showRightPants = true
        state.showLeftSleeve = true
        state.showRightSleeve = true
        state.showCape = false
    }

    override fun getTextureLocation(state: AvatarRenderState): Identifier {
        return DefaultPlayerSkin.getDefaultTexture()
    }
}
