package heckerpowered.matrix.client

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.network.MatrixClientPlayNetworking
import heckerpowered.matrix.client.render.ChannelSequenceRenderer
import heckerpowered.matrix.client.render.ScreenEffectRenderer
import heckerpowered.matrix.client.render.entity.MagicLightningEntityRenderer
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.MagicManager
import heckerpowered.matrix.common.entity.MatrixEntityType
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.entity.LivingEntity
import java.time.Duration

val minecraft
    get() = MinecraftClient.getInstance()!!

val world
    get() = MinecraftClient.getInstance().world!!

val player
    get() = MinecraftClient.getInstance().player!!

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
    }

    private fun registerEntityRenderers() {
        EntityRendererRegistry.register(MatrixEntityType.magicLightningEntity) { context ->
            MagicLightningEntityRenderer(context)
        }
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