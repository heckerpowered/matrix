/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.core.AimAssist
import heckerpowered.matrix.client.core.ClientOptions.aimAssistEnabled
import heckerpowered.matrix.client.core.ClientOptions.aimAssistFov
import heckerpowered.matrix.client.core.ClientOptions.aimAssistMaxDistance
import heckerpowered.matrix.client.ui.element.MagicList
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.item.LightningChestplate1.isBorrowedTime
import heckerpowered.matrix.common.magic.channel.CasterContext
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.system.Magics
import heckerpowered.matrix.common.network.ServerboundOverclockPayload
import heckerpowered.matrix.common.network.ServerboundUseMagicPayload
import heckerpowered.matrix.common.persistent.queueSize
import heckerpowered.matrix.core.getLerpedPos
import heckerpowered.matrix.core.toDegrees
import heckerpowered.matrix.core.utility.getEntitiesNearSight
import heckerpowered.matrix.core.wrapDegrees
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.util.ARGB
import net.minecraft.world.entity.LivingEntity
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.tan

object MatrixHud {
    private const val USE_MAGIC_REPEAT_INTERVAL_TICKS = 5
    private const val USE_MAGIC_REPEAT_INTERVAL_NANOS = USE_MAGIC_REPEAT_INTERVAL_TICKS * 50_000_000L
    private const val MAGIC_HUD_TIME_SCALE = 0.01
    private const val BORROWED_TIME_SCALE = 0.15

    var mana = .0
    var maxMana = .0
    var manaUsage = .0
    var isInfiniteMana = false

    var renderHud = true
    var useBloom = false
    var useBlur = false

    var targetedEntity: LivingEntity? = null
    private var candidateTargets: List<LivingEntity> = emptyList()
    private var manaOverclock = 1.0
    private var magicOverclock = 1.0
    private var previousMagicListVisible = false
    private var magicListKeyDown = false

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
    private val crosshairX = SimpleDoubleAnimation(initValue = .0)
    private val crosshairY = SimpleDoubleAnimation(initValue = .0)
    private val magicHudTimeScale = TimeController.allocateTimeController()
    private val lightningTimeScale = TimeController.allocateTimeController()

    private var selectedMagicIndex = 0
    private var crosshairTargetPosition: Vector2f? = null
    private var crosshairAnimationInitialized = false
    private var lastUseMagicNanos = 0L
    private var useMagicHoldArmed = false

    val selectedMagic: Magic
        get() {
            val magics = currentMagicList()
            selectedMagicIndex = selectedMagicIndex.coerceIn(0, (magics.size - 1).coerceAtLeast(0))
            return magics.getOrNull(selectedMagicIndex) ?: Magics.all.first()
        }

    fun onInitialize() {
        AimAssist.autoApplyRotation = false
        HudElementRegistry.addLast(Matrix.identifier("matrix_hud")) { drawContext, tickCounter ->
            onHudRender(drawContext, tickCounter)
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            handleKeyBindings()
        }
    }

    private fun handleKeyBindings() {
        if (minecraft.player == null || minecraft.level == null) {
            forceCloseMagicList(instant = true)
            return
        }

        if (minecraft.gui.screen() != null && magicListKeyDown) {
            forceCloseMagicList(instant = true)
            return
        }

        val hudActive = shouldRenderMagicList()
        updateMagicListVisibility(hudActive)

        while (MatrixKeyBindings.nextMagic.consumeClick()) {
            nextMagic()
        }
        while (MatrixKeyBindings.previousMagic.consumeClick()) {
            previousMagic()
        }
        var useMagicPressedWhileHudActive = false
        while (MatrixKeyBindings.useMagic.consumeClick()) {
            if (hudActive) {
                useMagicPressedWhileHudActive = true
            }
        }

        if (!MatrixKeyBindings.useMagic.isDown) {
            useMagicHoldArmed = false
        }
        if (useMagicPressedWhileHudActive) {
            useMagicHoldArmed = true
            lastUseMagicNanos = 0L
        }

        if (hudActive && MatrixKeyBindings.useMagic.isDown && useMagicHoldArmed) {
            val now = System.nanoTime()
            if (lastUseMagicNanos == 0L || now - lastUseMagicNanos >= USE_MAGIC_REPEAT_INTERVAL_NANOS) {
                useSelectedMagic()
                lastUseMagicNanos = now
            }
        } else {
            lastUseMagicNanos = 0L
            if (!hudActive) {
                useMagicHoldArmed = false
            }
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
            forceCloseMagicList(instant = true)
            return
        }
        if (minecraft.gui.screen() != null) {
            forceCloseMagicList(instant = true)
            return
        }
        syncMagicListKeyStateFromWindow()
        val hudActive = shouldRenderMagicList()
        updateMagicListVisibility(hudActive)
        if (!hudActive) {
            useMagicHoldArmed = false
            lastUseMagicNanos = 0L
        }

        val tickDelta = tickCounter.getGameTimeDeltaPartialTick(false)
        updateTargeting(tickDelta, drawContext.guiWidth(), drawContext.guiHeight())
        if (hudActive) {
            handleKeyBindings()
        }

        val availableMana = (mana - manaUsage).coerceAtLeast(.0)

        val magicListAlpha = magicListOpacity.animatedValue
        updateTimeScale(hudActive)
        if (magicListAlpha > 0.01) {
            renderManaBar(drawContext, availableMana, magicListAlpha)
            renderTargetGuides(drawContext, tickDelta, magicListAlpha)
            MagicList.render(
                drawContext = drawContext,
                magics = currentMagicList(),
                selectedIndex = selectedMagicIndex,
                target = targetedEntity,
                alpha = magicListAlpha,
                availableMana = availableMana,
                maxMana = maxMana,
                manaRate = manaOverclock,
                magicRate = magicOverclock,
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
        return MatrixClient.getPlayerMagics()
    }

    fun shouldSlowTime(): Boolean {
        val server = minecraft.singleplayerServer ?: return false
        return minecraft.isLocalServer && !server.isPublished
    }

    @JvmStatic
    fun onDoAttack() {
        if (isPressingRightMouseButton && shouldRenderMagicList()) {
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
        val targetPosition = crosshairTargetPosition
        if (targetPosition == null || !aimAssistEnabled) {
            crosshairX.animatedValue = x.toDouble()
            crosshairY.animatedValue = y.toDouble()
            crosshairAnimationInitialized = false
            return Vector2f(x, y)
        }

        if (!crosshairAnimationInitialized) {
            crosshairX.animatedValue = x.toDouble()
            crosshairY.animatedValue = y.toDouble()
            crosshairAnimationInitialized = true
        }

        crosshairX.value = targetPosition.x.toDouble()
        crosshairY.value = targetPosition.y.toDouble()
        return Vector2f(crosshairX.animatedValue.toFloat(), crosshairY.animatedValue.toFloat())
    }

    @JvmStatic
    fun onKey(window: Long, key: Int, scancode: Int, action: Int, mods: Int): Boolean {
        if (key != GLFW.GLFW_KEY_TAB || minecraft.player == null) {
            return false
        }
        if (minecraft.gui.screen() != null) {
            magicListKeyDown = false
            return false
        }

        val pressed = action != GLFW.GLFW_RELEASE
        if (pressed != magicListKeyDown) {
            magicListKeyDown = pressed
        }
        return true
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

    private fun useSelectedMagic(): Boolean {
        if (!shouldRenderMagicList()) {
            return false
        }
        val magic = selectedMagicOrNull() ?: return false
        val target = bestUsableTarget(magic) ?: return false
        ClientPlayNetworking.send(ServerboundUseMagicPayload(magic.definition.uuid, target.id))
        return true
    }

    private fun updateTimeScale(active: Boolean) {
        val canSlowTime = shouldSlowTime()
        magicHudTimeScale.value = if (active && canSlowTime) {
            MAGIC_HUD_TIME_SCALE
        } else {
            1.0
        }
        val hasBorrowedTime = minecraft.player?.isBorrowedTime == true
        lightningTimeScale.value = if (canSlowTime && hasBorrowedTime) {
            BORROWED_TIME_SCALE
        } else {
            1.0
        }
        TimeController.playerStandaloneRenderTick = canSlowTime && hasBorrowedTime && !active && !minecraft.isPaused
        TimeController.onRenderTick()
    }

    private fun resetTimeScale() {
        magicHudTimeScale.animatedValue = 1.0
        lightningTimeScale.animatedValue = 1.0
        TimeController.playerStandaloneRenderTick = false
        TimeController.onRenderTick()
    }

    private fun syncMagicListKeyStateFromWindow() {
        if (!magicListKeyDown) {
            return
        }
        val tabDown = GLFW.glfwGetKey(minecraft.window.handle(), GLFW.GLFW_KEY_TAB) == GLFW.GLFW_PRESS
        if (!tabDown) {
            magicListKeyDown = false
        }
    }

    private fun updateMagicListVisibility(visible: Boolean) {
        if (visible != previousMagicListVisible) {
            magicListOpacity.value = if (visible) 1.0 else .0
            previousMagicListVisible = visible
        }
    }

    private fun forceCloseMagicList(instant: Boolean) {
        magicListKeyDown = false
        previousMagicListVisible = false
        if (instant) {
            magicListOpacity.animatedValue = .0
        } else {
            magicListOpacity.value = .0
        }
        resetTimeScale()
    }

    private fun updateTargeting(tickDelta: Float, viewportWidth: Int, viewportHeight: Int) {
        val player = minecraft.player
        if (player == null || minecraft.level == null) {
            candidateTargets = emptyList()
            targetedEntity = null
            crosshairTargetPosition = null
            return
        }

        val directTarget = minecraft.crosshairPickEntity as? LivingEntity
        val canUseAssistedTarget = aimAssistEnabled && shouldRenderMagicList()
        val candidates = if (canUseAssistedTarget) {
            player.getEntitiesNearSight(aimAssistMaxDistance, aimAssistFov, tickDelta)
                .mapNotNull { it as? LivingEntity }
                .filter { it !== player && it.isAlive && !it.isSpectator }
                .take(8)
                .toList()
        } else {
            emptyList()
        }

        val magic = selectedMagicOrNull()
        candidateTargets = candidates
        targetedEntity = when {
            !canUseAssistedTarget -> directTarget
            directTarget != null && magic != null && canChannelMagicOn(directTarget, magic) -> directTarget
            directTarget != null && magic == null -> directTarget
            magic != null -> candidates.firstOrNull { canChannelMagicOn(it, magic) } ?: directTarget ?: candidates.firstOrNull()
            else -> directTarget ?: candidates.firstOrNull()
        }

        val target = targetedEntity
        crosshairTargetPosition = if (canUseAssistedTarget && target != null) {
            val targetPosition = target.getLerpedPos(tickDelta).add(.0, target.boundingBox.ysize * 0.5, .0)
            targetPosition.toCrosshairScreenPosition(tickDelta, viewportWidth, viewportHeight)
        } else {
            null
        }
    }

    private fun renderManaBar(drawContext: GuiGraphicsExtractor, availableMana: Double, alpha: Double) {
        val width = drawContext.guiWidth()
        val barLeft = 18
        val barRight = width - 18
        val barTop = 9
        val barBottom = 17
        val ratio = if (maxMana <= .0) .0 else (availableMana / maxMana).coerceIn(.0, 1.0)
        val fillRight = barLeft + ((barRight - barLeft) * ratio).roundToInt()

        drawContext.fill(barLeft, barTop, barRight, barBottom, color((alpha * 128).roundToInt(), 2, 8, 14))
        drawContext.fill(barLeft, barTop, fillRight, barBottom, color((alpha * 218).roundToInt(), 58, 167, 255))
        drawContext.outline(barLeft, barTop, barRight - barLeft, barBottom - barTop, color((alpha * 190).roundToInt(), 140, 226, 255))

        val manaText = "${availableMana.toInt()}/${max(maxMana, .0).toInt()}"
        drawContext.centeredText(minecraft.font, manaText, width / 2, 20, color((alpha * 235).roundToInt(), 234, 246, 255))
    }

    private fun renderTargetGuides(drawContext: GuiGraphicsExtractor, tickDelta: Float, magicListAlpha: Double) {
        if (candidateTargets.isEmpty() || magicListAlpha <= 0.01) {
            return
        }

        val alpha = (magicListAlpha * 220).roundToInt()
        val points = candidateTargets.mapNotNull { entity ->
            val targetPosition = entity.getLerpedPos(tickDelta).add(.0, entity.boundingBox.ysize * 0.62, .0)
            val screenPosition = targetPosition.toCrosshairScreenPosition(
                tickDelta,
                drawContext.guiWidth(),
                drawContext.guiHeight(),
                centered = true,
            ) ?: return@mapNotNull null
            entity to screenPosition
        }

        points.zipWithNext().forEach { (from, to) ->
            drawLine(drawContext, from.second.x.toDouble(), from.second.y.toDouble(), to.second.x.toDouble(), to.second.y.toDouble(), color((alpha * 0.7).roundToInt(), 118, 216, 255))
        }

        points.forEachIndexed { index, (entity, screenPosition) ->
            val selected = entity === targetedEntity
            val color = if (selected) {
                color(alpha, 118, 216, 255)
            } else {
                color((alpha * 0.55).roundToInt(), 76, 136, 165)
            }

            val x = screenPosition.x.roundToInt()
            val y = screenPosition.y.roundToInt()
            val radius = if (selected) 9 else 6
            drawTargetBracket(drawContext, x, y, radius, color)
        }
    }

    private fun selectedMagicOrNull(): Magic? {
        val magics = currentMagicList()
        selectedMagicIndex = selectedMagicIndex.coerceIn(0, (magics.size - 1).coerceAtLeast(0))
        return magics.getOrNull(selectedMagicIndex)
    }

    private fun bestUsableTarget(magic: Magic): LivingEntity? {
        val directTarget = minecraft.crosshairPickEntity as? LivingEntity
        if (directTarget != null && canChannelMagicOn(directTarget, magic)) {
            return directTarget
        }
        if (targetedEntity != null && canChannelMagicOn(targetedEntity, magic)) {
            return targetedEntity
        }
        return candidateTargets.firstOrNull { canChannelMagicOn(it, magic) }
    }

    private fun canChannelMagicOn(target: LivingEntity?, magic: Magic): Boolean {
        val player = minecraft.player ?: return false
        target ?: return false
        val queue = target.getChannelQueue(player)
        if (queue?.isLocked == true) {
            return false
        }
        if (queue?.isChanneling == true && queue.queuedMagicCount >= player.queueSize) {
            return false
        }

        val context = MagicCalculationContext(
            caster = CasterContext.fromEntity(player),
            target = target,
            queue = queue,
        )
        return magic.availableStatus(context).isAvailable
    }

    private fun net.minecraft.world.phys.Vec3.toCrosshairScreenPosition(
        tickDelta: Float,
        viewportWidth: Int,
        viewportHeight: Int,
        centered: Boolean = false,
    ): Vector2f? {
        val player = minecraft.player ?: return null
        val eyePosition = player.getEyePosition(tickDelta)
        val direction = subtract(eyePosition)
        val distance2D = sqrt(direction.x * direction.x + direction.z * direction.z)
        if (distance2D <= 0.0001 && abs(direction.y) <= 0.0001) {
            return null
        }

        val pitch = -toDegrees(atan2(direction.y, distance2D))
        val yaw = toDegrees(atan2(direction.z, direction.x)) - 90.0
        val yawDifference = wrapDegrees(yaw - player.getYRot(tickDelta).toDouble())
        val pitchDifference = wrapDegrees(pitch - player.getXRot(tickDelta).toDouble())

        val verticalFov = minecraft.options.fov().get().toDouble().coerceIn(30.0, 110.0)
        val aspectRatio = viewportWidth.toDouble() / viewportHeight.toDouble().coerceAtLeast(1.0)
        val horizontalFov = toDegrees(2.0 * atan2(tan(java.lang.Math.toRadians(verticalFov) / 2.0) * aspectRatio, 1.0))

        val x = viewportWidth / 2.0 +
            tan(java.lang.Math.toRadians(yawDifference)) / tan(java.lang.Math.toRadians(horizontalFov) / 2.0) * viewportWidth / 2.0
        val y = viewportHeight / 2.0 +
            tan(java.lang.Math.toRadians(pitchDifference)) / tan(java.lang.Math.toRadians(verticalFov) / 2.0) * viewportHeight / 2.0

        val halfSize = if (centered) 0.0 else 7.0
        val maxX = viewportWidth - if (centered) 0.0 else 15.0
        val maxY = viewportHeight - if (centered) 0.0 else 15.0
        return Vector2f(
            (x - halfSize).coerceIn(0.0, maxX).toFloat(),
            (y - halfSize).coerceIn(0.0, maxY).toFloat(),
        )
    }

    private fun drawLine(drawContext: GuiGraphicsExtractor, x1: Double, y1: Double, x2: Double, y2: Double, color: Int) {
        val steps = max(abs(x2 - x1), abs(y2 - y1)).roundToInt().coerceAtLeast(1)
        for (step in 0..steps) {
            val t = step.toDouble() / steps.toDouble()
            val x = (x1 + (x2 - x1) * t).roundToInt()
            val y = (y1 + (y2 - y1) * t).roundToInt()
            drawContext.fill(x, y, x + 1, y + 1, color)
        }
    }

    private fun drawTargetBracket(drawContext: GuiGraphicsExtractor, x: Int, y: Int, radius: Int, color: Int) {
        val length = (radius * 0.7).roundToInt().coerceAtLeast(4)
        drawContext.horizontalLine(x - radius, x - radius + length, y - radius, color)
        drawContext.verticalLine(x - radius, y - radius, y - radius + length, color)
        drawContext.horizontalLine(x + radius - length, x + radius, y - radius, color)
        drawContext.verticalLine(x + radius, y - radius, y - radius + length, color)
        drawContext.horizontalLine(x - radius, x - radius + length, y + radius, color)
        drawContext.verticalLine(x - radius, y + radius - length, y + radius, color)
        drawContext.horizontalLine(x + radius - length, x + radius, y + radius, color)
        drawContext.verticalLine(x + radius, y + radius - length, y + radius, color)
    }

    private fun shouldRenderMagicList(): Boolean {
        return minecraft.player != null &&
            minecraft.gui.screen() == null &&
            currentMagicList().isNotEmpty() &&
            magicListKeyDown
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

private fun color(alpha: Int, red: Int, green: Int, blue: Int): Int {
    return ARGB.color(alpha.coerceIn(0, 255), red.coerceIn(0, 255), green.coerceIn(0, 255), blue.coerceIn(0, 255))
}
