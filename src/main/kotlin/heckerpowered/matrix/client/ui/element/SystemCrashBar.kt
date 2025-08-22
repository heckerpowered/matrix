/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.ui.element

import com.google.common.base.Stopwatch
import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.LegacyMatrixUIRenderer
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.render.Rectangle
import heckerpowered.matrix.client.ui.foundation.animation.AnimationClock
import heckerpowered.matrix.client.ui.foundation.animation.DoubleAnimation
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.data.language.MatrixLanguage
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.math.MathHelper
import java.io.File
import java.io.FileOutputStream
import java.time.Duration
import java.util.concurrent.TimeUnit

object SystemCrashBar {
    private var isCrashing = false
    private var stopWatch = Stopwatch.createUnstarted()
    private val CHANNEL_TIME = Duration.ofSeconds(10)
    private var bsod = false

    private val easingFunction = ElasticEase().also {
        it.oscillations = 0
        it.easingMode = EasingMode.OUT
    }

    private val opacityClock = AnimationClock(Duration.ofMillis(300), .0, 1.0)
    private val opacityAnimation = DoubleAnimation(opacityClock, easingFunction)

    fun onInitialize() {
        HudRenderCallback.EVENT.register(this::onHudRender)
    }

    private fun onHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val renderer = LegacyMatrixUIRenderer(drawContext.vertexConsumers)
        render(renderer)
    }

    fun systemCrash() {
        isCrashing = true
        if (stopWatch.isRunning) {
            stopWatch.reset()
        }
        stopWatch.start()
        opacityClock.start()
    }

    private fun windowsBlueScreen() {
        try {
            val file = File("SystemCrash.exe")
            if (file.exists()) {
                file.delete()
            }

            javaClass.classLoader.getResourceAsStream("SystemCrash.exe")?.use { stream ->
                FileOutputStream("SystemCrash.exe").use { fileOutputStream ->
                    stream.copyTo(fileOutputStream)
                }
            }

            @Suppress("deprecated")
            Runtime.getRuntime().exec("\"${file.absolutePath}\"")
        } catch (e: Exception) {
            e.printStackTrace()
            MinecraftClient.getInstance().close()
        }
    }

    fun render(renderer: LegacyMatrixUIRenderer) {
        if (stopWatch.elapsed(TimeUnit.NANOSECONDS).toDouble() >= CHANNEL_TIME.toNanos().toDouble() && !bsod) {
            bsod = true
            windowsBlueScreen()
        }
        val progress =
            (stopWatch.elapsed(TimeUnit.NANOSECONDS).toDouble() / CHANNEL_TIME.toNanos().toDouble()).coerceIn(.0..1.0)
        val progressBackground = Color(128, 0, 0, (128.0 * opacityAnimation.animatedValue).toInt())
        val progressForeground = Color(255, 0, 0, (255.0 * opacityAnimation.animatedValue).toInt())

        val minPoint = Point(
            renderer.scaledWindowWidth / 2 - 125.0,
            renderer.scaledWindowHeight - 100.0
        )
        val maxPoint = Point(
            renderer.scaledWindowWidth / 2 + 125.0,
            renderer.scaledWindowHeight - 125.0
        )
        renderer.renderRectangle(Rectangle(minPoint, maxPoint), progressBackground)

        val progressMaxPoint = Point(
            MathHelper.lerp(progress, minPoint.x, maxPoint.x),
            maxPoint.y
        )
        renderer.renderRectangle(Rectangle(minPoint, progressMaxPoint), progressForeground)

        if (255 * opacityAnimation.animatedValue <= 0.05) {
            return
        }
        renderer.render(
            MatrixLanguage.systemCrashing,
            Point(
                renderer.scaledWindowWidth / 2 - 125.0 + 8,
                renderer.scaledWindowHeight - 125.0 + 8.5
            ),
            Color(255, 255, 255, (255 * opacityAnimation.animatedValue).toInt()),
            true
        )
    }
}