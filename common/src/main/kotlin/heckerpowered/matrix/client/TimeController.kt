/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.network.ServerboundWarpPayload
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.DeltaTracker
import net.minecraft.util.Util
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

object TimeController {
    private val timeControllers = mutableSetOf<SimpleDoubleAnimation>()
    private var previousMinScale = .0

    private var lastFrameTime = Duration.ZERO
    var deltaTime = Duration.ZERO
        private set
    val strictDeltaTime
        get() = deltaTime * previousMinScale

    fun allocateTimeController(): SimpleDoubleAnimation {
        val controller = SimpleDoubleAnimation(1.0, 1.0)
        controller.value = 1.0
        timeControllers.add(controller)
        return controller
    }

    @JvmField
    var standaloneRenderTickCounter: DeltaTracker.Timer =
        DeltaTracker.Timer(20.0f, 0L, FloatUnaryOperator.identity())

    // 26.2: Camera.tickFov (was GameRenderer.updateFovMultiplier) and Minecraft.handleKeybinds (was
    // handleInputEvents) are private with no public equivalent (former access widener entries), so
    // they are accessed via reflection.
    // Fail soft: a lookup failure only degrades the standalone tick's FOV/keybind updates
    // instead of killing TimeController's static init (and with it the whole time system).
    private val tickFovMethod = runCatching {
        Camera::class.java.getDeclaredMethod("tickFov").apply { isAccessible = true }
    }.onFailure { heckerpowered.matrix.Matrix.LOGGER.error("Camera.tickFov reflection failed", it) }.getOrNull()
    private val handleKeybindsMethod = runCatching {
        Minecraft::class.java.getDeclaredMethod("handleKeybinds").apply { isAccessible = true }
    }.onFailure { heckerpowered.matrix.Matrix.LOGGER.error("Minecraft.handleKeybinds reflection failed", it) }.getOrNull()

    private const val VANILLA_NANOS_PER_TICK = 50_000_000L

    val isTimeScaled: Boolean
        get() = (Minecraft.getInstance().level?.tickRateManager()?.nanosecondsPerTick ?: VANILLA_NANOS_PER_TICK) > VANILLA_NANOS_PER_TICK

    /**
     * 26.2: DeltaTracker.Timer.msPerTick is final and the effective client pace is
     * `max(msPerTick, level.tickRateManager().millisecondsPerTick())` (Minecraft.getTickTargetMillis),
     * so the pace is driven through the client level's TickRateManager. The public
     * nanosecondsPerTick field is written directly because setTickRate clamps to >= 1 tps and
     * the 0.01x slow-time needs 0.2 tps; slow-time only ever slows below 20 tps, which the
     * max() in getTickTargetMillis passes through.
     */
    private fun setClientPace(timeScale: Double) {
        val level = Minecraft.getInstance().level ?: return
        level.tickRateManager().nanosecondsPerTick = (VANILLA_NANOS_PER_TICK / timeScale).toLong()
    }

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
        setClientPace(timeScale)
        ClientPlayNetworking.send(ServerboundWarpPayload(timeScale, playerStandaloneRenderTick))
    }

    private fun setClientTimeScale(timeScale: Double) {
        setClientPace(timeScale)
    }

    fun setPlayerStandaloneTimeScale(timeScale: Double) {
        setClientPace(timeScale)
    }

    @JvmStatic
    fun beginRenderTick(timeMillis: Long, tick: Boolean) {
        if (lastFrameTime != Duration.ZERO) {
            deltaTime = System.nanoTime().nanoseconds - lastFrameTime
        }
        lastFrameTime = System.nanoTime().nanoseconds
        // 26.2: beginRenderTick(ms, tick) became advanceGameTime(ms); vanilla now applies the tick flag
        // outside the call (see Minecraft.runTick), which is mirrored here.
        val ticks = if (tick) standaloneRenderTickCounter.advanceGameTime(Util.getMillis()) else 0
        if (minecraft.level == null || minecraft.player == null) {
            return
        }
        if (!playerStandaloneRenderTick) {
            return
        }
        for (i in 0..<ticks) {
            world.tickNonPassenger(player)
            tickFovMethod?.invoke(minecraft.gameRenderer.mainCamera())
            handleKeybindsMethod?.invoke(minecraft)
            minecraft.gameRenderer.itemInHandRenderer.tick()
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