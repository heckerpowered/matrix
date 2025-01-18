package heckerpowered.matrix.client.core

import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.core.lerp
import heckerpowered.matrix.core.toDegrees
import heckerpowered.matrix.core.wrapDegrees
import net.minecraft.util.math.Vec3d
import java.time.Duration
import kotlin.math.atan2
import kotlin.math.sqrt

object AimAssist {
    private val lockedYaw = SimpleDoubleAnimation(duration = Duration.ofMillis(300))
    private val lockedPitch = SimpleDoubleAnimation(duration = Duration.ofMillis(300))

    @JvmStatic
    fun onMouseUpdate(timeDelta: Double) {
        lockedPitch.from = player.pitch.toDouble()
        lockedYaw.from = player.yaw.toDouble()
    }

    fun applyRotation() {
        player.pitch = lockedPitch.animatedValue.toFloat()
        player.yaw = lockedYaw.animatedValue.toFloat()
    }

    fun lookAt(position: Vec3d, tickDelta: Double) {
        val x = lerp(tickDelta, player.prevX, player.x)
        val y = lerp(tickDelta, player.prevY, player.y)
        val z = lerp(tickDelta, player.prevZ, player.z)

        val playerPosition = Vec3d(x, y, z)

        val direction = position.subtract(playerPosition)
        val distance2D = sqrt(direction.x * direction.x + direction.z * direction.z)

        val pitch = -toDegrees(atan2(direction.y, distance2D))
        val yaw = toDegrees(atan2(direction.z, direction.x)) - 90

        lockedYaw.value = wrapDegrees(yaw)
        lockedPitch.value = wrapDegrees(pitch)
    }

    fun rotationDifference(position: Vec3d, tickDelta: Double): Double {
        val x = lerp(tickDelta, player.prevX, player.x)
        val y = lerp(tickDelta, player.prevY, player.y)
        val z = lerp(tickDelta, player.prevZ, player.z)

        val playerPosition = Vec3d(x, y, z)

        val direction = position.subtract(playerPosition)
        val distance2D = sqrt(direction.x * direction.x + direction.z * direction.z)

        val pitch = -toDegrees(atan2(direction.y, distance2D))
        val yaw = toDegrees(atan2(direction.z, direction.x)) - 90

        val pitchDifference = wrapDegrees(pitch) - wrapDegrees(player.pitch.toDouble())
        val yawDifference = wrapDegrees(yaw) - wrapDegrees(player.yaw.toDouble())
        return sqrt(pitchDifference * pitchDifference + yawDifference * yawDifference)
    }

    fun resetAnimation() {
        lockedPitch.start()
        lockedYaw.start()
    }
}