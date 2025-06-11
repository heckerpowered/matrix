package heckerpowered.matrix.client

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.network.MatrixClientPlayNetworking
import heckerpowered.matrix.client.render.ChannelSequenceRenderer
import heckerpowered.matrix.client.render.ScreenEffectRenderer
import heckerpowered.matrix.client.render.entity.EmptyRenderer
import heckerpowered.matrix.client.render.entity.FinderArrowEntityRenderer
import heckerpowered.matrix.client.render.entity.MagicLightningEntityRenderer
import heckerpowered.matrix.client.render.item.VortexItemRenderer
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.MagicManager
import heckerpowered.matrix.common.entity.MatrixEntityType
import heckerpowered.matrix.common.item.MagicTalismanItem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.entity.LivingEntity
import org.joml.Matrix4f
import java.time.Duration

val minecraft
    get() = MinecraftClient.getInstance()!!

val world
    get() = MinecraftClient.getInstance().world!!

val player
    get() = MinecraftClient.getInstance().player!!

val projectionMatrix: Matrix4f
    get() {
        val gameRenderer = minecraft.gameRenderer
        val tickDelta = minecraft.renderTickCounter.getTickDelta(true)
        val projectionMatrix = gameRenderer.getBasicProjectionMatrix(gameRenderer.getFov(gameRenderer.camera, tickDelta, true))
        return projectionMatrix
    }

val modelViewMatrix: Matrix4f
    get() {
        val camera = minecraft.gameRenderer.camera
        return Matrix4f().apply {
            identity()
            set(camera.rotation.conjugate())
            translate(
                (-camera.pos.x).toFloat(),
                (-camera.pos.y).toFloat(),
                (-camera.pos.z).toFloat()
            )
        }
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

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register { _, entityRenderer, registrationHelper, _ ->
            @Suppress("UNCHECKED_CAST")
            registrationHelper.register(ChannelSequenceRenderer(entityRenderer as FeatureRendererContext<LivingEntity, EntityModel<LivingEntity>>))
        }

        BuiltinItemRendererRegistry.INSTANCE.register(MagicTalismanItem, VortexItemRenderer)
    }

    private fun registerEntityRenderers() {
        EntityRendererRegistry.register(MatrixEntityType.MAGIC_LIGHTNING_ENTITY) { context -> MagicLightningEntityRenderer(context) }
        EntityRendererRegistry.register(MatrixEntityType.ATTRACTOR_ENTITY) { context -> EmptyRenderer(context) }
        EntityRendererRegistry.register(MatrixEntityType.FINDER_ARROW_ENTITY) { context -> FinderArrowEntityRenderer(context) }
    }

    companion object {
        private var lastNonEmptyMagicList: List<Magic>? = null

        fun getPlayerMagics(): List<Magic> {
            val lastNonEmptyMagicList = lastNonEmptyMagicList
            val magics = MagicManager.getMagics(player)
            if (magics.isNotEmpty()) {
                this.lastNonEmptyMagicList = magics
            } else if (lastNonEmptyMagicList?.isNotEmpty() == true) {
                return lastNonEmptyMagicList
            }
            return magics
        }
    }
}