package heckerpowered.matrix.client

import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.network.WarpPayload
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Util
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

object TimeController {
    private val timeControllers = mutableSetOf<SimpleDoubleAnimation>()
    private var previousMinScale = .0

    private var lastFrameTime = Duration.ZERO
    var deltaTime = Duration.ZERO
        private set

    fun allocateTimeController(): SimpleDoubleAnimation {
        val controller = SimpleDoubleAnimation(1.0, 1.0)
        controller.value = 1.0
        timeControllers.add(controller)
        return controller
    }

    @JvmField
    var standaloneRenderTickCounter: RenderTickCounter.Dynamic =
        RenderTickCounter.Dynamic(20.0f, 0L, FloatUnaryOperator.identity())

    val isTimeScaled: Boolean
        get() = MinecraftClient.getInstance().renderTickCounter.tickTime - 50.0F > 0.001F

    @JvmStatic
    var playerImmuneTimeScale: Boolean = false

    /**
     * Whether the local player is using the standalone render tick, usually used for slow the world's
     * time but not affecting the player.
     */
    @JvmStatic
    var playerStandaloneRenderTick: Boolean = false
    private var previousPlayerStandaloneTickState = false

    private fun setTimeScale(timeScale: Double) {
        MinecraftClient.getInstance().renderTickCounter.tickTime = (50.0 / timeScale).toFloat()
        ClientPlayNetworking.send(WarpPayload(timeScale, playerStandaloneRenderTick))
    }

    private fun setClientTimeScale(timeScale: Double) {
        MinecraftClient.getInstance().renderTickCounter.tickTime = (50.0 / timeScale).toFloat()
    }

    fun setPlayerStandaloneTimeScale(timeScale: Double) {
        MinecraftClient.getInstance().renderTickCounter.tickTime = (50.0 / timeScale).toFloat()
    }

    @JvmStatic
    fun beginRenderTick(timeMillis: Long, tick: Boolean) {
        if (lastFrameTime != Duration.ZERO) {
            deltaTime = System.nanoTime().nanoseconds - lastFrameTime
        }
        lastFrameTime = System.nanoTime().nanoseconds
        val ticks = standaloneRenderTickCounter.beginRenderTick(Util.getMeasuringTimeMs(), tick)
        if (minecraft.world == null || minecraft.player == null) {
            return
        }
        if (!playerStandaloneRenderTick) {
            return
        }
        for (i in 0..<ticks) {
            world.tickEntity(player)
            minecraft.gameRenderer.updateFovMultiplier()
            minecraft.handleInputEvents()
            minecraft.gameRenderer.firstPersonRenderer.updateHeldItems()
        }
    }

    fun onRenderTick() {
        val minScale = timeControllers.minOf { it.value }
        if (minScale != previousMinScale || previousPlayerStandaloneTickState != playerStandaloneRenderTick) {
            setTimeScale(minScale)
        }

        previousMinScale = minScale
        previousPlayerStandaloneTickState = playerStandaloneRenderTick
        val minClientScale = timeControllers.minOf { it.animatedValue }
        setClientTimeScale(minClientScale)
    }

    val minTimeScale
        get() = timeControllers.minOf { it.value }
}