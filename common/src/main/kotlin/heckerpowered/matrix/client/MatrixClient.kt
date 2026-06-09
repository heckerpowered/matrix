/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.network.MatrixClientPlayNetworking
import heckerpowered.matrix.client.render.ChannelSequenceRenderer
import heckerpowered.matrix.client.render.MatrixGuiPipelines
import heckerpowered.matrix.client.render.MatrixPointSpriteParticles
import heckerpowered.matrix.client.render.MatrixRenderSystem
import heckerpowered.matrix.client.render.MatrixShaderPipelines
import heckerpowered.matrix.client.render.ScreenEffectRenderer
import heckerpowered.matrix.client.render.TargetGuideRenderer
import heckerpowered.matrix.client.render.entity.DevEntityRenderer
import heckerpowered.matrix.client.render.entity.EmptyRenderer
import heckerpowered.matrix.client.render.entity.FinderArrowEntityRenderer
import heckerpowered.matrix.client.render.entity.MagicLightningEntityRenderer
import heckerpowered.matrix.client.render.post.ShockwaveRenderer
import heckerpowered.matrix.client.shader.ShaderStageStore
import heckerpowered.matrix.client.ui.element.DamageNumberHud
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.common.entity.ModEntityTypes
import heckerpowered.matrix.common.entity.AttractorEntity
import heckerpowered.matrix.common.entity.DevEntity
import heckerpowered.matrix.common.entity.FinderArrowEntity
import heckerpowered.matrix.common.entity.MagicLightningBolt
import heckerpowered.matrix.common.item.getMagics
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.system.Magics
import heckerpowered.matrix.core.isInfiniteMana
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import org.joml.Matrix4f
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import java.time.Duration

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
        DamageNumberHud.onInitialize()
        ScreenEffectRenderer.onInitialize()
        MatrixPointSpriteParticles.onInitialize()
        ChannelSequenceRenderer.onInitialize()
        TargetGuideRenderer.onInitialize()
        ShockwaveRenderer.onInitialize()
        MatrixKeyBindings.onInitialize()
        registerShaderReloadListener()
        registerEntityRenderers()
        onWindowInitialization()
    }

    private fun registerEntityRenderers() {
        EntityRendererRegistry.register<MagicLightningBolt>(ModEntityTypes.MAGIC_LIGHTNING_ENTITY) { context -> MagicLightningEntityRenderer(context) }
        EntityRendererRegistry.register<AttractorEntity>(ModEntityTypes.attractor) { context -> EmptyRenderer(context) }
        EntityRendererRegistry.register<FinderArrowEntity>(ModEntityTypes.FINDER_ARROW_ENTITY) { context -> FinderArrowEntityRenderer(context) }
        EntityRendererRegistry.register<DevEntity>(ModEntityTypes.devEntity) { context -> DevEntityRenderer(context) }
    }

    private fun registerShaderReloadListener() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
            object : SimpleSynchronousResourceReloadListener {
                override fun getFabricId() = Matrix.identifier("shader_pipeline_reload")

                override fun onResourceManagerReload(resourceManager: ResourceManager) {
                    MatrixShaderPipelines.requestPostPipelinePrecompile()
                    MatrixGuiPipelines.requestGuiPipelinePrecompile()
                }
            }
        )
        ClientTickEvents.END_CLIENT_TICK.register {
            MatrixShaderPipelines.runRequestedPrecompileIfPossible()
            MatrixGuiPipelines.runRequestedPrecompileIfPossible()
        }
    }

    companion object {
        private var lastNonEmptyMagicList: List<Magic>? = null
        private var shaderPipelinesInitialized = false

        fun getPlayerMagics(): List<Magic> {
            val player = player ?: return lastNonEmptyMagicList ?: emptyList()
            val magics = if (MatrixHud.isInfiniteMana || player.isInfiniteMana) {
                Magics.all.toList()
            } else {
                player.getMagics().toList()
            }
            if (magics.isNotEmpty()) {
                this.lastNonEmptyMagicList = magics
            }
            return magics
        }

        @JvmStatic
        fun onWindowInitialization() {
            if (shaderPipelinesInitialized) {
                return
            }
            shaderPipelinesInitialized = true
            ShaderStageStore.Default.discoverFiles()
            ShaderStageStore.Default.precompileAll()
            MatrixShaderPipelines.registerKnownPostPipelines()
            MatrixGuiPipelines.registerKnownGuiPipelines()
        }
    }
}
