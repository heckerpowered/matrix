/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import com.mojang.blaze3d.platform.InputConstants
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.ui.element.MagicList
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.system.Magics
import heckerpowered.matrix.common.network.ServerboundOverclockPayload
import heckerpowered.matrix.common.network.ServerboundUseMagicPayload
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.entity.LivingEntity
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW

object MatrixHud {
    var mana = .0
    var maxMana = .0
    var manaUsage = .0

    var renderHud = true
    var useBloom = false
    var useBlur = false

    var targetedEntity: LivingEntity? = null
    private var manaOverclock = 1.0
    private var magicOverclock = 1.0
    private var previousMagicListVisible = false

    val manaOverclockValue: Double
        get() = manaOverclock

    val magicOverclockValue: Double
        get() = magicOverclock

    @JvmField
    var isPressingRightMouseButton = false

    @JvmField
    var isPressingLeftMouseButton = false

    @JvmField
    val fovAnimation = SimpleDoubleAnimation(initValue = 1.0)

    @JvmField
    val magicShownOpacityAnimation = SimpleDoubleAnimation(initValue = 1.0)

    private val magicListOpacity = SimpleDoubleAnimation(initValue = .0)

    private var selectedMagicIndex = 0

    val selectedMagic: Magic
        get() {
            val magics = MatrixClient.getPlayerMagics().ifEmpty { Magics.all.toList() }
            selectedMagicIndex = selectedMagicIndex.coerceIn(0, (magics.size - 1).coerceAtLeast(0))
            return magics.getOrNull(selectedMagicIndex) ?: Magics.all.first()
        }

    fun onInitialize() {
        HudElementRegistry.addLast(Matrix.identifier("matrix_hud")) { drawContext, tickCounter ->
            onHudRender(drawContext, tickCounter)
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            handleKeyBindings()
        }
    }

    private fun handleKeyBindings() {
        val magicListVisible = shouldRenderMagicList()
        if (magicListVisible != previousMagicListVisible) {
            magicListOpacity.value = if (magicListVisible) 1.0 else .0
            previousMagicListVisible = magicListVisible
        }

        while (MatrixKeyBindings.nextMagic.consumeClick()) {
            nextMagic()
        }
        while (MatrixKeyBindings.previousMagic.consumeClick()) {
            previousMagic()
        }
        while (MatrixKeyBindings.useMagic.consumeClick()) {
            useSelectedMagic()
        }
        while (MatrixKeyBindings.overclockMagic.consumeClick()) {
            if (minecraft.player?.isShiftKeyDown == true) {
                underclockMagic()
            } else {
                overclockMagic()
            }
        }
        while (MatrixKeyBindings.overclockMana.consumeClick()) {
            if (minecraft.player?.isShiftKeyDown == true) {
                underclockMana()
            } else {
                overclockMana()
            }
        }
    }

    private fun onHudRender(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        if (!shouldRenderHud() || !renderHud) {
            return
        }
        targetedEntity = minecraft.crosshairPickEntity as? LivingEntity

        val width = drawContext.guiWidth()
        val height = drawContext.guiHeight()
        val barWidth = 96
        val barHeight = 6
        val x = 12
        val y = height - 28
        val availableMana = (mana - manaUsage).coerceAtLeast(.0)
        val ratio = if (maxMana <= .0) .0 else (availableMana / maxMana).coerceIn(.0, 1.0)

        drawContext.fill(x, y, x + barWidth, y + barHeight, 0x88000000.toInt())
        drawContext.fill(x, y, x + (barWidth * ratio).toInt(), y + barHeight, 0xCC3AA7FF.toInt())
        drawContext.text(minecraft.font, "${availableMana.toInt()}/${maxMana.toInt()}", x, y - 11, 0xFFEAF6FF.toInt())

        val magicName = runCatching { selectedMagic.definition.name.string }.getOrElse { "" }
        if (magicName.isNotBlank()) {
            drawContext.text(minecraft.font, magicName, width - minecraft.font.width(magicName) - 12, height - 22, 0xFFFFFFFF.toInt())
        }
        val overclockText = "Mana x${formatOverclock(manaOverclock)}  Magic x${formatOverclock(magicOverclock)}"
        drawContext.text(minecraft.font, overclockText, x, y + 10, 0xFFE0F2FF.toInt())

        val magicListAlpha = magicListOpacity.animatedValue
        if (magicListAlpha > 0.01) {
            MagicList.render(
                drawContext = drawContext,
                magics = currentMagicList(),
                selectedIndex = selectedMagicIndex,
                target = targetedEntity,
                alpha = magicListAlpha,
            )
        }
    }

    @JvmStatic
    fun onRemoteManaUpdate() {
        manaUsage = manaUsage.coerceAtMost(mana)
    }

    @JvmStatic
    fun shouldRenderHud(): Boolean {
        return minecraft.player != null
    }

    fun currentMagicList(): List<Magic> {
        return MatrixClient.getPlayerMagics().ifEmpty { Magics.all.toList() }
    }

    @JvmStatic
    fun onDoAttack() {
        if (isPressingRightMouseButton) {
            useSelectedMagic()
        }
    }

    @JvmStatic
    fun nextMagic() {
        val size = currentMagicList().size
        if (size > 0) selectedMagicIndex = (selectedMagicIndex + 1).floorMod(size)
    }

    @JvmStatic
    fun previousMagic() {
        val size = currentMagicList().size
        if (size > 0) selectedMagicIndex = (selectedMagicIndex - 1).floorMod(size)
    }

    @JvmStatic
    fun nextZoomLevel() {
        fovAnimation.value = 0.75
    }

    @JvmStatic
    fun previousZoomLevel() {
        fovAnimation.value = 1.0
    }

    @JvmStatic
    fun overclockMana() {
        manaOverclock = (manaOverclock + 0.5).coerceIn(1.0, 10.0)
        syncOverclock()
    }

    @JvmStatic
    fun underclockMana() {
        manaOverclock = (manaOverclock - 0.5).coerceIn(1.0, 10.0)
        syncOverclock()
    }

    @JvmStatic
    fun overclockMagic() {
        magicOverclock = (magicOverclock + 0.5).coerceIn(1.0, 10.0)
        syncOverclock()
    }

    @JvmStatic
    fun underclockMagic() {
        magicOverclock = (magicOverclock - 0.5).coerceIn(1.0, 10.0)
        syncOverclock()
    }

    @JvmStatic
    fun translateCrosshairPosition(x: Float, y: Float): Vector2f {
        return Vector2f(x, y)
    }

    @JvmStatic
    fun onKey(window: Long, key: Int, scancode: Int, action: Int, mods: Int): Boolean {
        return false
    }

    @JvmStatic
    fun onMouseButton(window: Long, button: Int, action: Int, mods: Int): Boolean {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            isPressingRightMouseButton = action != GLFW.GLFW_RELEASE
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            isPressingLeftMouseButton = action != GLFW.GLFW_RELEASE
        }
        return false
    }

    private fun useSelectedMagic() {
        val target = targetedEntity ?: minecraft.crosshairPickEntity as? LivingEntity ?: return
        val magic = runCatching { selectedMagic }.getOrNull() ?: return
        ClientPlayNetworking.send(ServerboundUseMagicPayload(magic.definition.uuid, target.id))
    }

    private fun shouldRenderMagicList(): Boolean {
        val window = minecraft.window
        return minecraft.player != null &&
            currentMagicList().isNotEmpty() &&
            InputConstants.isKeyDown(window, GLFW.GLFW_KEY_TAB)
    }

    private fun Int.floorMod(modulo: Int): Int {
        return ((this % modulo) + modulo) % modulo
    }

    private fun formatOverclock(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }

    private fun syncOverclock() {
        if (minecraft.player == null || minecraft.connection == null) {
            return
        }
        ClientPlayNetworking.send(ServerboundOverclockPayload(manaOverclock, magicOverclock))
    }
}
