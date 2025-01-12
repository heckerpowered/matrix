package heckerpowered.matrix.client

import heckerpowered.matrix.common.network.WrapPayload
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Util

/**
 *
 */
object Wrap {
    @JvmStatic
    var renderTickCounter: RenderTickCounter.Dynamic = RenderTickCounter.Dynamic(20.0f, 0L, FloatUnaryOperator.identity())

    val isTimeScaled: Boolean
        get() = MinecraftClient.getInstance().renderTickCounter.tickTime != 50.0f

    @JvmStatic
    var playerImmuneTimeScale: Boolean = false

    @JvmStatic
    var timeScaled: Boolean = false

    fun setTimeScale(timeScale: Double) {
        timeScaled = timeScale != 1.0
        MinecraftClient.getInstance().renderTickCounter.tickTime = (50.0 / timeScale).toFloat()
        ClientPlayNetworking.send(WrapPayload(timeScale))
    }

    fun setClientTimeScale(timeScale: Double) {
        MinecraftClient.getInstance().renderTickCounter.tickTime = (50.0 / timeScale).toFloat()
    }

    fun tick() {
        renderTickCounter.beginRenderTick(Util.getMeasuringTimeMs(), true)
        val minecraftClient = MinecraftClient.getInstance()!!
        val player = minecraftClient.player!!
        val world = player.world!!
        val tickCount = renderTickCounter.beginRenderTick(Util.getMeasuringTimeMs(), playerImmuneTimeScale && timeScaled)
        for (i in 0 until tickCount.coerceAtMost(10)) {
            player.tick()
        }
    }
}