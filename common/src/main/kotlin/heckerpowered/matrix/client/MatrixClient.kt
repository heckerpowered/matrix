/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.network.MatrixClientPlayNetworking
import heckerpowered.matrix.client.render.ChannelSequenceRenderer
import heckerpowered.matrix.client.render.MatrixRenderSystem
import heckerpowered.matrix.client.render.dump
import heckerpowered.matrix.client.render.mainRenderTarget
import heckerpowered.matrix.client.render.ScreenEffectRenderer
import heckerpowered.matrix.client.render.entity.DevEntityRenderer
import heckerpowered.matrix.client.render.entity.EmptyRenderer
import heckerpowered.matrix.client.render.entity.FinderArrowEntityRenderer
import heckerpowered.matrix.client.render.entity.MagicLightningEntityRenderer
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.common.entity.ModEntityTypes
import heckerpowered.matrix.common.item.getMagics
import heckerpowered.matrix.common.magic.core.Magic
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.Minecraft
import org.joml.Matrix4f
import java.time.Duration

// Restored from pre-migration MatrixClient.kt (accidentally deleted in 53dcdd6);
// same non-null semantics as the original 1.21 definitions.
val minecraft
    get() = Minecraft.getInstance()!!

val world
    get() = Minecraft.getInstance().level!!

val player
    get() = Minecraft.getInstance().player!!

val projectionMatrix: Matrix4f
    get() {
        // Basic projection calculation:
        // val gameRenderer = minecraft.gameRenderer
        // val tickDelta = minecraft.renderTickCounter.getTickDelta(true)
        // val projectionMatrix = gameRenderer.getBasicProjectionMatrix(gameRenderer.getFov(gameRenderer.camera, tickDelta, true))
        // return projectionMatrix
        return MatrixRenderSystem.projectionMatrix
    }

val viewMatrix: Matrix4f
    get() {
        // val camera = minecraft.gameRenderer.camera
        // val cameraPosition = camera.pos.toVector3f()
        // val cameraRotation = camera.rotation
//
        // return Matrix4f()
        //     .rotate(cameraRotation.conjugate(Quaternionf()))
        //     .translate(-cameraPosition)

        return MatrixRenderSystem.viewMatrix
    }

val animationDuration: Duration = Duration.ofMillis(300)

val easingFunction = ElasticEase().also {
    it.oscillations = 0
    it.easingMode = EasingMode.OUT
}

class MatrixClient : ClientModInitializer {
    override fun onInitializeClient() {
        Matrix.proxy = MatrixClientProxy()

        MatrixHud.onInitialize()
        MatrixClientPlayNetworking.onInitialize()
        ScreenEffectRenderer.onInitialize()
        MatrixKeyBindings.onInitialize()
        registerEntityRenderers()

        // 26.2: ChannelSequenceRenderer is no longer a per-entity-type RenderLayer registered
        // through LivingEntityRenderLayerRegistrationCallback — see the structural-decision
        // note at the top of ChannelSequenceRenderer.kt for why (no Fabric API extraction hook
        // exists to get the source LivingEntity into an arbitrary LivingEntityRenderState
        // without a Mixin). It is now a self-contained HUD overlay; onInitialize() registers
        // both its ClientTickEvents animation driver and its HudElementRegistry draw callback.
        ChannelSequenceRenderer.onInitialize()

        // 26.2: BuiltinItemRendererRegistry/DynamicItemRenderer (Fabric API) is gone; custom
        // item GPU rendering now goes through the data-driven SpecialModelRenderer system
        // (item model JSON + registered Unbaked/codec type), which is out of scope for this
        // renderer-only port pass. See the TODO(26.2) note at the top of VortexItemRenderer.kt.

    }

    private fun registerEntityRenderers() {
        EntityRendererRegistry.register(ModEntityTypes.MAGIC_LIGHTNING_ENTITY) { context -> MagicLightningEntityRenderer(context) }
        EntityRendererRegistry.register(ModEntityTypes.attractor) { context -> EmptyRenderer(context) }
        EntityRendererRegistry.register(ModEntityTypes.FINDER_ARROW_ENTITY) { context -> FinderArrowEntityRenderer(context) }
        EntityRendererRegistry.register(ModEntityTypes.devEntity) { context -> DevEntityRenderer(context) }
    }

    companion object {
        private var lastNonEmptyMagicList: List<Magic>? = null

        fun getPlayerMagics(): List<Magic> {
            // The old MagicManager/MagicSystem lookup was replaced by the Player.getMagics()
            // extension during the magic-system refactor; same enchantment-derived list.
            val magics = player.getMagics().toList()
            if (magics.isNotEmpty()) {
                this.lastNonEmptyMagicList = magics
            }
            return magics
        }

        @JvmStatic
        fun onWindowInitialization() {
            // 26.2: shader pipelines are compiled by the vanilla ShaderManager on demand
        }
    }
}