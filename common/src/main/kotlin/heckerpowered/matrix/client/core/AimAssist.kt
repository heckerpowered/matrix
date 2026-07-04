/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.core

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.core.lerp
import heckerpowered.matrix.core.toDegrees
import heckerpowered.matrix.core.wrapDegrees
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.world.phys.Vec3
import java.time.Duration
import kotlin.math.atan2
import kotlin.math.sqrt

object AimAssist {
    private val lockedYaw = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))
    private val lockedPitch = SimpleDoubleAnimation(duration = Duration.ofMillis(1000))

    var autoApplyRotation = false
    val isAiming: Boolean
        get() = lockedYaw.isAnimating || lockedPitch.isAnimating

    /**
     * Whether the mouse input is locked when using aim assist.
     */
    var isMouseLocked = false

    /**
     * Automatically set *isMouseLocked* to false when aim assist is not active.
     */
    var autoUnlock = true

    init {
        // 26.2: HudRenderCallback was replaced by HudElementRegistry; still invoked once per rendered frame.
        HudElementRegistry.addLast(Matrix.identifier("aim_assist")) { drawContext, tickCounter ->
            if (autoApplyRotation && isAiming) {
                applyRotation()
            }
            if (autoUnlock && isMouseLocked && !isAiming) {
                isMouseLocked = false
            }
        }
    }

    @JvmStatic
    fun onMouseUpdate(timeDelta: Double): Boolean {
        lockedPitch.from = player.getViewXRot(timeDelta.toFloat()).toDouble()
        lockedYaw.from = player.getViewYRot(timeDelta.toFloat()).toDouble()
        return isMouseLocked && isAiming
    }

    fun applyRotation() {
        player.xRot = lockedPitch.animatedValue.toFloat()
        player.yRot = lockedYaw.animatedValue.toFloat()
    }

    fun lookAt(position: Vec3, tickDelta: Double) {
        val x = lerp(tickDelta, player.xo, player.x)
        val y = lerp(tickDelta, player.yo, player.y)
        val z = lerp(tickDelta, player.zo, player.z)

        val playerPosition = Vec3(x, y, z)

        val direction = position.subtract(playerPosition)
        val distance2D = sqrt(direction.x * direction.x + direction.z * direction.z)

        val pitch = -toDegrees(atan2(direction.y, distance2D))
        val yaw = toDegrees(atan2(direction.z, direction.x)) - 90

        lockedYaw.value = wrapDegrees(yaw)
        lockedPitch.value = wrapDegrees(pitch)
    }

    fun rotationDifference(position: Vec3, tickDelta: Double): Double {
        val x = lerp(tickDelta, player.xo, player.x)
        val y = lerp(tickDelta, player.yo, player.y)
        val z = lerp(tickDelta, player.zo, player.z)

        val playerPosition = Vec3(x, y, z)

        val direction = position.subtract(playerPosition)
        val distance2D = sqrt(direction.x * direction.x + direction.z * direction.z)

        val pitch = -toDegrees(atan2(direction.y, distance2D))
        val yaw = toDegrees(atan2(direction.z, direction.x)) - 90

        val pitchDifference = wrapDegrees(pitch) - wrapDegrees(player.xRot.toDouble())
        val yawDifference = wrapDegrees(yaw) - wrapDegrees(player.yRot.toDouble())
        return sqrt(pitchDifference * pitchDifference + yawDifference * yawDifference)
    }

    fun resetAnimation() {
        lockedPitch.start()
        lockedYaw.start()
    }
}