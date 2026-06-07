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
    private const val WARP_HEARTBEAT_NANOS = 500_000_000L
    private const val STANDALONE_TICK_MILLIS = 50.0F

    private val timeControllers = mutableSetOf<SimpleDoubleAnimation>()
    private var previousMinScale = 1.0
    private var clientTimeScale = 1.0
    private var lastWarpSyncNanos = 0L

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
        private var deltaTicks = .0F
        private var deltaTickResidual = .0F
        private var pausedDeltaTickResidual = .0F
        private var lastMillis = 0L
        private var paused = false
        private var frozen = false

        fun beginRenderTick(timeMillis: Long, tick: Boolean): Int {
            if (!tick) {
                return 0
            }
            if (lastMillis == 0L) {
                lastMillis = timeMillis
                return 0
            }

            val elapsedMillis = (timeMillis - lastMillis).coerceAtLeast(0L)
            lastMillis = timeMillis
            deltaTicks = elapsedMillis / STANDALONE_TICK_MILLIS
            deltaTickResidual += deltaTicks
            val ticks = deltaTickResidual.toInt()
            deltaTickResidual -= ticks.toFloat()
            return ticks
        }

        fun tick(paused: Boolean) {
            if (paused && !this.paused) {
                pausedDeltaTickResidual = deltaTickResidual
            } else if (!paused && this.paused) {
                deltaTickResidual = pausedDeltaTickResidual
            }
            this.paused = paused
        }

        fun setTickFrozen(frozen: Boolean) {
            this.frozen = frozen
        }

        fun getTickDelta(tick: Boolean): Float {
            if (!tick && frozen) {
                return 1.0F
            }
            return if (paused) pausedDeltaTickResidual else deltaTickResidual
        }
    }

    @JvmField
    var standaloneRenderTickCounter = StandaloneRenderTickCounter()

    val isTimeScaled: Boolean
        get() = clientTimeScale < 0.999 || previousMinScale != 1.0

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
        if (minecraft.connection == null) {
            return
        }
        ClientPlayNetworking.send(ServerboundWarpPayload(timeScale, playerStandaloneRenderTick))
        lastWarpSyncNanos = System.nanoTime()
    }

    private fun setClientTimeScale(timeScale: Double) {
        clientTimeScale = timeScale.coerceIn(0.01, 1.0)
    }

    fun setPlayerStandaloneTimeScale(timeScale: Double) {
        setClientTimeScale(timeScale)
    }

    @JvmStatic
    fun getClientGameTimeScale(): Float {
        return clientTimeScale.coerceIn(0.01, 1.0).toFloat()
    }

    @JvmStatic
    fun shouldScaleClientGameTime(): Boolean {
        return getClientGameTimeScale() < 0.999F
    }

    @JvmStatic
    fun scaleClientGameDelta(delta: Float): Float {
        if (clientTimeScale >= 0.999) {
            return delta
        }
        return (delta * clientTimeScale.toFloat()).coerceAtLeast(0.0F)
    }

    @JvmStatic
    fun beginRenderTick(timeMillis: Long, tick: Boolean) {
        if (lastFrameTime != Duration.ZERO) {
            deltaTime = System.nanoTime().nanoseconds - lastFrameTime
        }
        lastFrameTime = System.nanoTime().nanoseconds
        val standaloneTicks = standaloneRenderTickCounter.beginRenderTick(timeMillis, tick)
        val level = minecraft.level ?: return
        val player = minecraft.player ?: return
        if (!playerStandaloneRenderTick || minecraft.isPaused || standaloneTicks <= 0) {
            return
        }

        repeat(standaloneTicks.coerceAtMost(10)) {
            level.tickNonPassenger(player)
            minecraft.gameRenderer.tick()
        }
    }

    fun onRenderTick() {
        val minScale = timeControllers.minOf { it.value }
        val now = System.nanoTime()
        val shouldRefreshWarp = minScale < 1.0 && now - lastWarpSyncNanos >= WARP_HEARTBEAT_NANOS
        if (minScale != previousMinScale || previousPlayerStandaloneTickState != playerStandaloneRenderTick || shouldRefreshWarp) {
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
