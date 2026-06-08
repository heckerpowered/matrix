/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import heckerpowered.foundation.ui.color.Argb8
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.core.worldToScreen
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.util.ARGB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.time.Duration
import kotlin.math.round
import kotlin.math.roundToInt

object DamageNumberHud {
    private data class DamageNumber(
        val damage: Float,
        val color: Int,
        val position: Vec3,
        val size: SimpleDoubleAnimation,
        val yOffset: SimpleDoubleAnimation,
        val opacity: SimpleDoubleAnimation,
        val uid: Long,
    )

    private val damageNumbers = mutableListOf<DamageNumber>()
    private var counter = 0L

    fun onInitialize() {
        HudElementRegistry.addLast(Matrix.identifier("damage_numbers")) { drawContext, tickCounter ->
            onHudRender(drawContext, tickCounter)
        }
    }

    fun nextInstantKillMessage(uid: Long): String {
        return when (uid % 5) {
            0L -> "代码是这样写的"
            1L -> "跟我的保险说去吧"
            2L -> "我还以为是减速带呢"
            3L -> "我们都在用力的活着"
            4L -> "我都用力了你怎么还活着"
            else -> "114514"
        }
    }

    fun addDamageNumber(damage: Float, color: Any, position: Vec3) {
        val size = SimpleDoubleAnimation().apply {
            from = 20.0
            to = 4.0
            duration = Duration.ofMillis(300)
            start()
        }
        val yOffset = SimpleDoubleAnimation().apply {
            from = .0
            to = .5
            duration = Duration.ofMillis(900)
            start()
        }
        val opacity = SimpleDoubleAnimation(initValue = 255.0).apply {
            from = 255.0
            to = .0
            duration = Duration.ofMillis(900)
            startTime = Duration.ofMillis(300)
            start()
        }

        damageNumbers += DamageNumber(
            damage = damage,
            color = toPackedColor(color),
            position = position,
            size = size,
            yOffset = yOffset,
            opacity = opacity,
            uid = counter++,
        )
    }

    private fun onHudRender(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        if (damageNumbers.isEmpty()) {
            return
        }

        damageNumbers.forEach { damageNumber ->
            renderDamageNumber(drawContext, damageNumber)
        }
        damageNumbers.removeIf { it.opacity.animatedValue <= .0 }
    }

    private fun renderDamageNumber(drawContext: GuiGraphicsExtractor, damageNumber: DamageNumber) {
        val opacity = damageNumber.opacity.animatedValue
        if (opacity < 4.0) {
            return
        }

        val screenPosition = worldToScreen(
            damageNumber.position.add(.0, damageNumber.yOffset.animatedValue, .0),
            viewportWidth = drawContext.guiWidth(),
            viewportHeight = drawContext.guiHeight(),
        ) ?: return

        val text = damageNumber.text()
        val textWidth = minecraft.font.width(text)
        val textHeight = minecraft.font.lineHeight
        val textX = screenPosition.x.roundToInt()
        val textY = screenPosition.y.roundToInt()
        val centerX = textX + textWidth / 2.0F
        val centerY = textY + textHeight / 2.0F
        val scale = damageNumber.size.animatedValue.toFloat()
        val color = ARGB.color(
            opacity.roundToInt().coerceIn(0, 255),
            ARGB.red(damageNumber.color),
            ARGB.green(damageNumber.color),
            ARGB.blue(damageNumber.color),
        )

        val pose = drawContext.pose()
        pose.pushMatrix()
        pose.translate(centerX, centerY)
        pose.scale(scale, scale)
        pose.translate(-centerX, -centerY)
        drawContext.text(minecraft.font, text, textX, textY, color, true)
        pose.popMatrix()
    }

    private fun DamageNumber.text(): String {
        return when {
            damage.isInfinite() -> "9999"
            damage >= 1 -> round(damage).toULong().toString()
            else -> ((damage * 10.0).toULong().toDouble() / 10.0).toString()
        }
    }

    private fun toPackedColor(color: Any): Int {
        return when (color) {
            is Argb8 -> color.packed
            is Int -> color
            is Vector3f -> ARGB.color(
                255,
                color.x.toInt().coerceIn(0, 255),
                color.y.toInt().coerceIn(0, 255),
                color.z.toInt().coerceIn(0, 255),
            )
            else -> ARGB.white(255)
        }
    }
}
