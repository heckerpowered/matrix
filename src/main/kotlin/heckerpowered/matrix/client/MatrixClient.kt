package heckerpowered.matrix.client

import heckerpowered.matrix.client.network.MatrixClientPlayNetworking
import heckerpowered.matrix.client.render.ChannelSequenceRenderer
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.magics.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.PostEffectProcessor
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.entity.LivingEntity
import java.time.Duration

val minecraft
    get() = MinecraftClient.getInstance()!!

val player
    get() = MinecraftClient.getInstance().player!!

val animationDuration = Duration.ofMillis(200)

val easingFunction = ElasticEase().also {
    it.oscillations = 0
    it.easingMode = EasingMode.OUT
}

class MatrixClient : ClientModInitializer {
    var colorFilter: PostEffectProcessor? = null

    override fun onInitializeClient() {
        MatrixHud.onInitialize()
        MatrixClientPlayNetworking.onInitialize()

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register { _, entityRenderer, registrationHelper, _ ->
            @Suppress("UNCHECKED_CAST")
            registrationHelper.register(ChannelSequenceRenderer(entityRenderer as FeatureRendererContext<LivingEntity, EntityModel<LivingEntity>>))
        }
    }

    companion object {
        fun getPlayerMagics(): List<Magic> {
            return listOf(
                TargetPositioningMagic(),
                DecisiveStrikeMagic(),
                HealthStealMagic(),
                ManaOverloadMagic(),
                ExplosionMagic(),
                // KillMagic(),
                // SculkCatalystMagic(),
                IgniteMagic(),
                BreakingBadMagic(),
                CrippleMovementMagic(),
                MemoryEraseMagic(),
                SpreadMagic(),
                LightningBoltMagic(),
                TeleportMagic(),
                ArmorPenetrationMagic(),
                // SystemCrashMagic()
            )
        }
    }
}