/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import com.google.common.base.Stopwatch
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.render.Rectangle
import heckerpowered.matrix.client.ui.foundation.animation.AnimationClock
import heckerpowered.matrix.client.ui.foundation.animation.DoubleAnimation
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.data.language.MatrixLanguage
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.DeltaTracker
import net.minecraft.util.Mth
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
        // 26.2: HudRenderCallback was replaced by HudElementRegistry; still invoked once per rendered frame.
        HudElementRegistry.addLast(Matrix.identifier("system_crash_bar")) { drawContext, tickCounter ->
            onHudRender(drawContext, tickCounter)
        }
    }

    private fun onHudRender(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        render(drawContext)
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
            Minecraft.getInstance().close()
        }
    }

    // 26.2: LegacyMatrixUIRenderer needed a VertexConsumerProvider.Immediate, which
    // GuiGraphicsExtractor no longer exposes (immediate-mode drawing was removed). This now
    // draws directly through GuiGraphicsExtractor.fill()/text(), which is order-independent
    // on min/max like the old renderRectangle() quad, keeping the same rectangles and text.
    private fun renderRectangle(drawContext: GuiGraphicsExtractor, rectangle: Rectangle, color: Color) {
        drawContext.fill(
            rectangle.min.x.toInt(),
            rectangle.min.y.toInt(),
            rectangle.max.x.toInt(),
            rectangle.max.y.toInt(),
            color.toInt()
        )
    }

    fun render(drawContext: GuiGraphicsExtractor) {
        if (stopWatch.elapsed(TimeUnit.NANOSECONDS).toDouble() >= CHANNEL_TIME.toNanos().toDouble() && !bsod) {
            bsod = true
            windowsBlueScreen()
        }
        val progress =
            (stopWatch.elapsed(TimeUnit.NANOSECONDS).toDouble() / CHANNEL_TIME.toNanos().toDouble()).coerceIn(.0..1.0)
        val progressBackground = Color(128, 0, 0, (128.0 * opacityAnimation.animatedValue).toInt())
        val progressForeground = Color(255, 0, 0, (255.0 * opacityAnimation.animatedValue).toInt())

        val scaledWindowWidth = drawContext.guiWidth()
        val scaledWindowHeight = drawContext.guiHeight()

        val minPoint = Point(
            scaledWindowWidth / 2 - 125.0,
            scaledWindowHeight - 100.0
        )
        val maxPoint = Point(
            scaledWindowWidth / 2 + 125.0,
            scaledWindowHeight - 125.0
        )
        renderRectangle(drawContext, Rectangle(minPoint, maxPoint), progressBackground)

        val progressMaxPoint = Point(
            Mth.lerp(progress, minPoint.x, maxPoint.x),
            maxPoint.y
        )
        renderRectangle(drawContext, Rectangle(minPoint, progressMaxPoint), progressForeground)

        if (255 * opacityAnimation.animatedValue <= 0.05) {
            return
        }
        drawContext.text(
            minecraft.font,
            MatrixLanguage.systemCrashing,
            (scaledWindowWidth / 2 - 125.0 + 8).toInt(),
            (scaledWindowHeight - 125.0 + 8.5).toInt(),
            Color(255, 255, 255, (255 * opacityAnimation.animatedValue).toInt()).toInt(),
            true
        )
    }
}