/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.network.ServerboundWarpPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
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

    class StandaloneRenderTickCounter {
        fun beginRenderTick(timeMillis: Long, tick: Boolean): Int = if (tick) 1 else 0
        fun tick(paused: Boolean) = Unit
        fun setTickFrozen(frozen: Boolean) = Unit
        fun getTickDelta(tick: Boolean): Float = 1.0F
    }

    @JvmField
    var standaloneRenderTickCounter = StandaloneRenderTickCounter()

    val isTimeScaled: Boolean
        get() = previousMinScale != 1.0

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
        ClientPlayNetworking.send(ServerboundWarpPayload(timeScale, playerStandaloneRenderTick))
    }

    private fun setClientTimeScale(timeScale: Double) {
    }

    fun setPlayerStandaloneTimeScale(timeScale: Double) {
    }

    @JvmStatic
    fun beginRenderTick(timeMillis: Long, tick: Boolean) {
        if (lastFrameTime != Duration.ZERO) {
            deltaTime = System.nanoTime().nanoseconds - lastFrameTime
        }
        lastFrameTime = System.nanoTime().nanoseconds
        standaloneRenderTickCounter.beginRenderTick(timeMillis, tick)
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
