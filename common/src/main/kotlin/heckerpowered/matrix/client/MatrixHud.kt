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
import heckerpowered.matrix.client.event.KeyEvent
import heckerpowered.matrix.client.event.MouseButtonEvent
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.ui.element.ManaBar
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.item.LightningChestplate1
import heckerpowered.matrix.common.item.LightningChestplate1.isBorrowedTime
import heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking
import heckerpowered.matrix.common.item.WizardHelmet5
import heckerpowered.matrix.common.magic.channel.CasterContext
import heckerpowered.matrix.common.magic.channel.ChannelEntry
import heckerpowered.matrix.common.magic.channel.ChannelExecutor
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getOrCreateChannelQueue
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicAvailability
import heckerpowered.matrix.common.magic.core.MagicAvailableStatus
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.system.Magics
import heckerpowered.matrix.common.network.ServerboundActivateBloodPactPayload
import heckerpowered.matrix.common.network.ServerboundBorrowedTimePayload
import heckerpowered.matrix.common.network.ServerboundOverclockPayload
import heckerpowered.matrix.common.network.ServerboundUseMagicPayload
import heckerpowered.matrix.common.persistent.isWizard
import heckerpowered.matrix.common.persistent.queueSize
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import heckerpowered.matrix.core.getLerpedPos
import heckerpowered.matrix.core.utility.getEntitiesNearSight
import heckerpowered.matrix.core.worldToScreen
import heckerpowered.matrix.data.language.MatrixLanguage
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.ARGB
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

object MatrixHud {
    private const val USE_MAGIC_REPEAT_INTERVAL_TICKS = 5
    private const val MAGIC_HUD_TIME_SCALE = 0.01
    private const val BORROWED_TIME_SCALE = 0.15

    var mana
        get() = manaBar.mana.value
        set(value) {
            manaBar.mana.value = value.coerceIn(0.0..maxMana)
        }

    var maxMana
        get() = manaBar.maxMana.value.coerceAtLeast(
            .0
        )
        set(value) {
            manaBar.maxMana.value = value
        }

    var manaUsage
        get() = manaBar.manaUsage.value
        set(value) {
            manaBar.manaUsage.value = value
        }
    var isInfiniteMana = false

    var renderHud = true
    var useBloom = false
    var useBlur = false

    var targetedEntity: LivingEntity? = null
    private var candidateTargets: List<LivingEntity> = emptyList()
    private var usingRayCast = false
    private var manaOverclock = 1.0
    private var magicOverclock = 1.0
    private var previousMagicListVisible = false
    private var magicListKeyDown = false
    private var cachedTargetedEntity: LivingEntity? = null
    private var lastFrameNanos = System.nanoTime()

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
    val magicShownOpacityAnimation = SimpleDoubleAnimation(initValue = .0)

    private val magicShownAnimation = SimpleDoubleAnimation(initValue = -50.0)
    private val manaShownAnimation = SimpleDoubleAnimation(initValue = -50.0)
    private val manaOpacityAnimation = SimpleDoubleAnimation(initValue = .0)
    private val manaValueAnimation = SimpleDoubleAnimation(initValue = .0)
    private val maxManaValueAnimation = SimpleDoubleAnimation(initValue = .0)
    private val manaUsageAnimation = SimpleDoubleAnimation(initValue = .0)
    private val manaCostAnimation = SimpleDoubleAnimation(initValue = .0)
    private val manaCostTooltipShownAnimation = SimpleDoubleAnimation(initValue = .0)
    private val manaCostTooltipOpacityAnimation = SimpleDoubleAnimation(initValue = .0)
    private val manaCostTooltipRedAnimation = SimpleDoubleAnimation(initValue = .0)
    private val manaCostTooltipGreenAnimation = SimpleDoubleAnimation(initValue = .0)
    private val manaCostTooltipBlueAnimation = SimpleDoubleAnimation(initValue = .0)
    private val availableStatusShownAnimation = SimpleDoubleAnimation(initValue = -50.0)
    private val availableStatusOpacityAnimation = SimpleDoubleAnimation(initValue = .0)
    private val healthPercentageAnimation = SimpleDoubleAnimation(initValue = .0)
    private val healthAnimation = SimpleDoubleAnimation(initValue = .0)
    private val maxHealthAnimation = SimpleDoubleAnimation(initValue = .0)
    private val entityDescriptionOpacityAnimation = SimpleDoubleAnimation(initValue = .0)
    private val descriptionYOffsetAnimation = SimpleDoubleAnimation(initValue = -35.0)
    private val descriptionExtraHeightAnimation = SimpleDoubleAnimation(initValue = .0, duration = Duration.ofMillis(450))
    private val magicDescriptionChangedAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(150))
    private val displayEntityNameOpacityAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(150))
    private val displayCandidateCountOpacityAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(150))
    private val candidateCountPaddingAnimation = SimpleDoubleAnimation(initValue = .0)
    private val manaOverclockAnimation = SimpleDoubleAnimation(initValue = 1.0)
    private val magicOverclockAnimation = SimpleDoubleAnimation(initValue = 1.0)
    private val grayscaleIntensityAnimation = SimpleDoubleAnimation(initValue = .0)
    private val crosshairX = SimpleDoubleAnimation(initValue = .0)
    private val crosshairY = SimpleDoubleAnimation(initValue = .0)
    private val magicHudTimeScale = TimeController.allocateTimeController()
    private val lightningTimeScale = TimeController.allocateTimeController()

    private var selectedMagicIndex = 0
    private var crosshairTargetPosition: Vector2f? = null
    private var crosshairAnimationInitialized = false
    private var lastUseMagicTick = Int.MIN_VALUE
    private var useMagicHoldArmed = false
    private var fovZoomRatio = .0
    private var currentDescription: Component = Component.empty()
    private var displayDescription: Component = Component.empty()
    private var previousDisplayEntityNameKey = ""
    private var displayEntityName: Component = Component.empty()
    private var previousCandidateEntityCount = 0
    private var displayCandidateEntityCount = 0
    private var currentAvailableStatusKey = "available"
    private var displayAvailableStatus: Component = MatrixLanguage.magicAvailable
    private val availableStatusChangedAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(150))
    private var lastManaCostTooltipState = false
    private var displayManaCostTooltipState = false
    private var lastManaCostTooltipDifference = 0L
    private var displayManaCostTooltipDifference = 0L
    private val manaCostTooltipStateChangedAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(150))
    private val manaCostTooltipDifferenceChangedAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(150))
    private val usingMagicList = mutableMapOf<Int, Double>()
    private var cachedMagicDisplayData = mutableListOf<MagicDisplayData>()
    private val magicColorAnimations = mutableListOf<HudColorAnimation>()
    private val magicExtraWidthAnimations = mutableListOf<SimpleDoubleAnimation>()
    private val theWorldShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/the_world.fsh", GL_FRAGMENT_SHADER),
        )
    }

    val grayscaleIntensity: Float
        get() = grayscaleIntensityAnimation.animatedValue.toFloat().coerceIn(.0F, 1.0F)

    private class HudColorAnimation {
        val red = SimpleDoubleAnimation(initValue = .0)
        val green = SimpleDoubleAnimation(initValue = .0)
        val blue = SimpleDoubleAnimation(initValue = .0)

        fun setColor(red: Double, green: Double, blue: Double) {
            this.red.value = red
            this.green.value = green
            this.blue.value = blue
        }

        fun setColorWithoutAnimation(red: Double, green: Double, blue: Double) {
            this.red.animatedValue = red
            this.green.animatedValue = green
            this.blue.animatedValue = blue
        }
    }

    private class MagicDisplayData {
        val statusChangedAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(150))
        val costChangedAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(150))
        val costWidthAnimation = SimpleDoubleAnimation(initValue = .0)

        var displayCost = 0L
        var previousCost = Long.MIN_VALUE
        var displayStatus: Component = MatrixLanguage.magicAvailable
        var previousStatusKey = ""
    }

    private data class MagicStatusDisplay(
        val key: String,
        val text: Component,
        val available: Boolean,
        val targetMissing: Boolean,
    )

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
        val player = minecraft.player
        if (player == null || minecraft.level == null) {
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
            lastUseMagicTick = player.tickCount - USE_MAGIC_REPEAT_INTERVAL_TICKS
        }

        if (hudActive && MatrixKeyBindings.useMagic.isDown && useMagicHoldArmed) {
            val currentTick = player.tickCount
            if (currentTick - lastUseMagicTick >= USE_MAGIC_REPEAT_INTERVAL_TICKS && useSelectedMagic()) {
                lastUseMagicTick = currentTick
            }
        } else {
            lastUseMagicTick = Int.MIN_VALUE
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
        if (!hasHudContext() || !renderHud) {
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
            lastUseMagicTick = Int.MIN_VALUE
        }

        val tickDelta = tickCounter.getGameTimeDeltaPartialTick(false)
        updateTargeting(tickDelta, drawContext.guiWidth(), drawContext.guiHeight(), hudActive)
        if (hudActive) {
            handleKeyBindings()
        }

        updateTimeScale(hudActive)
        updateTheWorldPostEffect()
        checkMagicAvailableStatus()
        performChannelMagicAnimation()

        val alpha = magicShownOpacityAnimation.animatedValue
        if (alpha <= 0.001) {
            magicExtraWidthAnimations.forEach { it.animatedValue = .0 }
            lastFrameNanos = System.nanoTime()
            return
        }

        renderManaCostTooltip(drawContext)
        renderLeftPart(drawContext)
        renderRightPart(drawContext)
        renderLegacyManaBar(drawContext)
        renderMagicAvailableStatus(drawContext, hudActive)

        if (hudActive) {
            renderOverclock(drawContext)
        }
        lastFrameNanos = System.nanoTime()
    }

    @JvmStatic
    fun onRemoteManaUpdate() {
        manaUsage = manaUsage.coerceAtMost(mana)
        manaBar.onRemoteManaUpdate()
    }

    @JvmStatic
    fun shouldRenderHud(): Boolean {
        return shouldRenderMagicList()
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
        fovZoomRatio = (fovZoomRatio + 0.2).coerceAtMost(0.95)
        fovAnimation.value = 1.0 - fovZoomRatio
    }

    @JvmStatic
    fun previousZoomLevel() {
        fovZoomRatio = (fovZoomRatio - 0.2).coerceAtLeast(.0)
        fovAnimation.value = 1.0 - fovZoomRatio
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
            if (!crosshairAnimationInitialized) {
                crosshairX.animatedValue = x.toDouble()
                crosshairY.animatedValue = y.toDouble()
            } else {
                crosshairX.value = x.toDouble()
                crosshairY.value = y.toDouble()
            }
            return Vector2f(crosshairX.animatedValue.toFloat(), crosshairY.animatedValue.toFloat())
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
        if (window != minecraft.window.handle() || minecraft.player == null) {
            return false
        }

        val screenOpen = minecraft.gui.screen() != null
        if (key == GLFW.GLFW_KEY_TAB) {
            if (screenOpen) {
                magicListKeyDown = false
                return false
            }

            val pressed = action != GLFW.GLFW_RELEASE
            if (pressed != magicListKeyDown) {
                magicListKeyDown = pressed
            }
            return true
        }

        if (!screenOpen && key == GLFW.GLFW_KEY_E && action != GLFW.GLFW_RELEASE) {
            val player = minecraft.player ?: return false
            val tabPressed = magicListKeyDown ||
                    GLFW.glfwGetKey(minecraft.window.handle(), GLFW.GLFW_KEY_TAB) == GLFW.GLFW_PRESS
            if (tabPressed && player.getItemBySlot(EquipmentSlot.CHEST).item is LightningChestplate1) {
                ClientPlayNetworking.send(ServerboundBorrowedTimePayload)
                return true
            }

            if (shouldRenderHud()) {
                ClientPlayNetworking.send(ServerboundActivateBloodPactPayload)
                return true
            }
        }

        if (!KeyEvent.EVENT.invoker().onKey(key, scancode, action, mods)) {
            return true
        }
        return false
    }

    @JvmStatic
    fun onMouseButton(window: Long, button: Int, action: Int, mods: Int): Boolean {
        if (window != minecraft.window.handle()) {
            return false
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            isPressingRightMouseButton = action != GLFW.GLFW_RELEASE
            if (shouldRenderHud()) {
                if (action == GLFW.GLFW_PRESS) {
                    fovZoomRatio = (fovZoomRatio + 0.2).coerceAtMost(0.95)
                    fovAnimation.value = 1.0 - fovZoomRatio
                    return true
                }
                if (action == GLFW.GLFW_RELEASE) {
                    fovZoomRatio = .0
                    fovAnimation.value = 1.0
                    return true
                }
            } else if (action == GLFW.GLFW_RELEASE) {
                fovZoomRatio = .0
                fovAnimation.value = 1.0
            }
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            isPressingLeftMouseButton = action != GLFW.GLFW_RELEASE
        }
        if (!MouseButtonEvent.EVENT.invoker().onMouseButton(button, action, mods)) {
            return true
        }
        return false
    }

    private fun useSelectedMagic(): Boolean {
        if (!shouldRenderMagicList()) {
            return false
        }
        val usedMagicIndex = selectedMagicIndex
        val magic = selectedMagicOrNull() ?: return false
        val target = bestUsableTarget(magic) ?: return false
        val player = minecraft.player ?: return false
        val queue = target.getOrCreateChannelQueue(player)
        val calculationContext = MagicCalculationContext(
            caster = CasterContext.fromEntity(player),
            target = target,
            queue = queue,
        )
        if (!magic.availableStatus(calculationContext).isAvailable) {
            return false
        }
        val cost = runCatching { magic.getCost(calculationContext) }.getOrDefault(magic.getNormalCost())
        val channelTime = runCatching { magic.getChannelTime(calculationContext) }.getOrDefault(magic.getNormalChannelTime())
        val availableMana = mana - manaUsage
        val level = minecraft.level
        if (player.isBloodPactActive && availableMana < cost) {
            level?.playSound(player, player.x, player.y, player.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F)
            level?.playSound(player, player.x, player.y, player.z, SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 1.0F)
        } else {
            level?.playSound(player, player.x, player.y, player.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F)
        }
        val predictedEntry = ChannelEntry(magic, cost, channelTime).also {
            it.clientPrediction = true
        }
        queue.enqueue(predictedEntry)
        magic.channel(MagicInvocation.fromEntity(player, target))
        ChannelExecutor.performChannelAnimation(predictedEntry, target, channelTime)
        ClientPlayNetworking.send(ServerboundUseMagicPayload(magic.definition.uuid, target.id))
        magicColorAnimations.getOrNull(usedMagicIndex)?.setColorWithoutAnimation(.0, 255.0, .0)
        usingMagicList[usedMagicIndex] = .0
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
        grayscaleIntensityAnimation.animatedValue = .0
        PostProcessRenderer.postProcessShaders.remove(theWorldShader)
    }

    private fun updateTheWorldPostEffect() {
        val player = minecraft.player
        val targetIntensity = when {
            player?.isPhaseWalking == true -> 1.0
            shouldSlowTime() -> magicShownOpacityAnimation.animatedValue.coerceIn(.0, 1.0)
            else -> .0
        }
        grayscaleIntensityAnimation.animatedValue = targetIntensity

        if (targetIntensity > 0.001) {
            PostProcessRenderer.postProcessShaders.add(theWorldShader)
        } else {
            PostProcessRenderer.postProcessShaders.remove(theWorldShader)
        }
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
            onHudVisibilityChanged(visible)
            previousMagicListVisible = visible
        }
    }

    private fun forceCloseMagicList(instant: Boolean) {
        magicListKeyDown = false
        previousMagicListVisible = false
        if (instant) {
            magicShownOpacityAnimation.animatedValue = .0
            magicShownAnimation.animatedValue = -50.0
            manaOpacityAnimation.animatedValue = .0
            manaShownAnimation.animatedValue = -50.0
            manaCostTooltipOpacityAnimation.animatedValue = .0
            manaCostTooltipShownAnimation.animatedValue = .0
            availableStatusOpacityAnimation.animatedValue = .0
            availableStatusShownAnimation.animatedValue = -50.0
            entityDescriptionOpacityAnimation.animatedValue = .0
            descriptionYOffsetAnimation.animatedValue = -35.0
        } else {
            onHudHide()
        }
        resetTimeScale()
    }

    private fun onHudVisibilityChanged(visible: Boolean) {
        if (visible) {
            onHudShown()
        } else {
            onHudHide()
        }
    }

    private fun onHudShown() {
        magicShownAnimation.value = .0
        magicShownOpacityAnimation.value = 1.0
        manaShownAnimation.value = .0
        manaOpacityAnimation.value = 1.0
        entityDescriptionOpacityAnimation.value = if (targetedEntity != null) 1.0 else .0
        descriptionYOffsetAnimation.value = if (targetedEntity != null) .0 else -35.0
        AimAssist.resetAnimation()

        manaBar.onHudVisibilityChanged(true)
    }

    var takeScreenShot = false

    private fun onHudHide() {
        magicShownAnimation.value = -50.0
        magicShownOpacityAnimation.value = .0
        manaShownAnimation.value = -50.0
        manaOpacityAnimation.value = .0
        manaCostTooltipOpacityAnimation.value = .0
        manaCostTooltipShownAnimation.value = .0
        availableStatusOpacityAnimation.value = .0
        availableStatusShownAnimation.value = -50.0
        entityDescriptionOpacityAnimation.value = .0
        descriptionYOffsetAnimation.value = -35.0
        fovAnimation.value = 1.0

        // takeScreenShot = true
        manaBar.onHudVisibilityChanged(false)
    }

    private fun updateTargeting(tickDelta: Float, viewportWidth: Int, viewportHeight: Int, hudActive: Boolean) {
        val player = minecraft.player
        if (player == null || minecraft.level == null || !hudActive) {
            candidateTargets = emptyList()
            targetedEntity = null
            crosshairTargetPosition = null
            usingRayCast = false
            return
        }

        val previousTarget = targetedEntity
        val magic = selectedMagicOrNull()
        val directTarget = getDirectTargetEntity(tickDelta, magic)
        val candidates = if (aimAssistEnabled && magic != null) {
            getAssistTargetEntities(tickDelta, magic)
        } else {
            emptyList()
        }
        candidateTargets = candidates
        usingRayCast = directTarget != null && directTarget !== minecraft.crosshairPickEntity.asLivingTarget()
        targetedEntity = when {
            magic != null && directTarget != null && canChannelMagicOn(directTarget, magic) -> directTarget
            magic != null -> candidates.firstOrNull()
            else -> directTarget ?: candidates.firstOrNull()
        }

        if (targetedEntity != previousTarget) {
            if (targetedEntity == null) {
                entityDescriptionOpacityAnimation.value = .0
                descriptionYOffsetAnimation.value = -35.0
            } else {
                entityDescriptionOpacityAnimation.value = 1.0
                descriptionYOffsetAnimation.value = .0
            }
        }
        if (targetedEntity != null) {
            cachedTargetedEntity = targetedEntity
        }

        val target = targetedEntity
        crosshairTargetPosition = if (aimAssistEnabled && target != null) {
            val targetPosition = target.getLerpedPos(tickDelta).add(.0, target.boundingBox.ysize * 0.5, .0)
            targetPosition.toGuiScreenPosition(tickDelta, viewportWidth, viewportHeight)
        } else {
            null
        }
    }

    private fun getDirectTargetEntity(tickDelta: Float, magic: Magic?): LivingEntity? {
        val player = minecraft.player ?: return null
        val vanillaTarget = minecraft.crosshairPickEntity.asLivingTarget()
        if (magic == null || player.wizardHelmetStack.item !is WizardHelmet5) {
            return vanillaTarget
        }

        return getRayCastTargetEntity(tickDelta, magic)?.asLivingTarget() ?: vanillaTarget
    }

    private fun getAssistTargetEntities(tickDelta: Float, magic: Magic): List<LivingEntity> {
        val player = minecraft.player ?: return emptyList()
        val entities = minecraft.cameraEntity?.getEntitiesNearSight(
            aimAssistMaxDistance,
            aimAssistFov,
            tickDelta,
        ) ?: return emptyList()

        return entities
            .mapNotNull { it as? LivingEntity }
            .filter { it !== player && it.isAlive && !it.isSpectator }
            .filter { canChannelMagicOn(it, magic) }
            .toList()
    }

    private fun getRayCastTargetEntity(tickDelta: Float, magic: Magic): Entity? {
        val player = minecraft.player ?: return null
        val cameraEntity = minecraft.cameraEntity ?: return null
        val range = aimAssistMaxDistance

        val location = minecraft.gameRenderer.mainCamera().position()
        val rotation = cameraEntity.getViewVector(tickDelta).normalize()
        val min = location.add(rotation)
        val max = location.add(rotation.scale(range))
        val box = AABB(min, max)

        val basePredicate = { entity: Entity ->
            val target = entity.asLivingTarget()
            entity !== cameraEntity &&
                    !entity.isSpectator &&
                    target != null &&
                    target.isAlive &&
                    canChannelMagicOn(target, magic)
        }

        return ProjectileUtil.getEntityHitResult(cameraEntity, min, max, box, basePredicate, range)?.entity
    }

    private val manaBar = ManaBar()
    private fun renderLegacyManaBar(drawContext: GuiGraphicsExtractor) {
        val player = minecraft.player
        val calculationContext = MagicCalculationContext.fromEntity(player, targetedEntity)
        val currentMagicCost = selectedMagicOrNull()
            ?.let { magic ->
                runCatching { magic.getCost(calculationContext) }
                    .getOrDefault(magic.getNormalCost())
            }
            ?: 0L

        manaBar.maxMana.value = maxMana.coerceAtLeast(.0)
        manaBar.mana.value = mana.coerceAtLeast(.0)
        manaBar.manaUsage.value = manaUsage.coerceAtLeast(.0)
        manaBar.manaCost.value = currentMagicCost.toDouble().coerceAtLeast(.0)

        manaBar.render(drawContext)
    }

    fun targetGuideChain(): List<LivingEntity> {
        val result = mutableListOf<LivingEntity>()
        val seen = mutableSetOf<Int>()

        fun addTarget(entity: LivingEntity?) {
            if (entity == null || !entity.isAlive || !seen.add(entity.id)) {
                return
            }
            result.add(entity)
        }

        addTarget(targetedEntity)
        candidateTargets.forEach(::addTarget)
        return result
    }

    private fun renderLeftPart(drawContext: GuiGraphicsExtractor) {
        val magics = currentMagicList()
        if (magics.isEmpty()) {
            return
        }

        fillColorAnimationList(magics)
        fillExtraWidthAnimationList(magics)
        ensureMagicDisplayData(magics)

        val indentList = generateIndentList(magics.size)
        magics.forEachIndexed { index, magic ->
            renderMagic(drawContext, index, magic, indentList)
        }
    }

    private fun renderMagic(
        drawContext: GuiGraphicsExtractor,
        index: Int,
        magic: Magic,
        indentList: List<Double>,
    ) {
        val displayData = magicDisplayData[index]
        val xIndent = indentList[index].roundToInt()
        val animatedColor = magicColorAnimations.getOrNull(index)
        val alpha = magicShownOpacityAnimation.animatedValue
        val backgroundColor = color(
            (alpha * 127.5).roundToInt(),
            animatedColor?.red?.animatedValue?.roundToInt() ?: 0,
            animatedColor?.green?.animatedValue?.roundToInt() ?: 0,
            animatedColor?.blue?.animatedValue?.roundToInt() ?: 0,
        )

        val rowHeight = 20.0
        val margin = 5.0
        val startY = (index + 1) * (rowHeight + margin) + drawContext.guiHeight() / 2.0 - (indentList.size + 1) * (rowHeight + margin) / 2.0
        val top = startY.roundToInt()
        val bottom = (startY + rowHeight).roundToInt()

        val status = getMagicStatusDisplay(magic, targetMissingAsAvailable = true)
        val calculationContext = MagicCalculationContext.fromEntity(minecraft.player, targetedEntity)
        val normalCost = runCatching { magic.getNormalCost() }.getOrDefault(magic.definition.baseCost.toDouble().toLong())
        val cost = runCatching { magic.getCost(calculationContext) }.getOrDefault(normalCost)

        if (displayData.previousCost != cost) {
            displayData.previousCost = cost
            displayData.displayCost = cost
            displayData.costChangedAnimation.animatedValue = .0
            displayData.costChangedAnimation.value = 1.0
        }
        if (displayData.previousStatusKey != status.key) {
            displayData.previousStatusKey = status.key
            displayData.displayStatus = status.text
            displayData.statusChangedAnimation.animatedValue = .0
            displayData.statusChangedAnimation.value = 1.0
        }

        val costString = when {
            displayData.displayCost > normalCost -> "§c↑§r${displayData.displayCost}"
            displayData.displayCost < normalCost -> "§a↓§r${displayData.displayCost}"
            else -> displayData.displayCost.toString()
        }

        val magicNameWidth = minecraft.font.width(magic.definition.name)
        displayData.costWidthAnimation.value = minecraft.font.width(costString).toDouble()
        val costStringWidth = displayData.costWidthAnimation.animatedValue
        val statusStringWidth = minecraft.font.width(displayData.displayStatus)
        val extraWidth = magicNameWidth + costStringWidth + statusStringWidth + 30.0
        val extraWidthAnimation = magicExtraWidthAnimations[index]
        extraWidthAnimation.value = extraWidth

        val offsetX = magicShownAnimation.animatedValue.roundToInt()
        val left = xIndent + 50 + offsetX
        val right = xIndent + 50 + extraWidthAnimation.animatedValue.roundToInt() + offsetX

        drawContext.enableScissor(left, top, right, bottom)
        drawContext.fill(left, top, right, bottom, backgroundColor)

        val foregroundColor = color((alpha * 255).roundToInt(), 255, 255, 255)
        if (alpha > 0.016) {
            drawContext.text(minecraft.font, magic.definition.name, xIndent + 55 + offsetX, top + 5, foregroundColor)
        }

        val costAlpha = min(alpha, displayData.costChangedAnimation.animatedValue)
        if (costAlpha > 0.016) {
            drawContext.text(
                minecraft.font,
                costString,
                xIndent + 65 + magicNameWidth + offsetX,
                top + 5,
                color((costAlpha * 255).roundToInt(), 255, 255, 255),
            )
        }

        val statusAlpha = min(alpha, displayData.statusChangedAnimation.animatedValue)
        if (statusAlpha > 0.016) {
            drawContext.text(
                minecraft.font,
                displayData.displayStatus,
                xIndent + 75 + magicNameWidth + costStringWidth.roundToInt() + offsetX,
                top + 5,
                color((statusAlpha * 255).roundToInt(), 255, 255, 255),
            )
        }
        drawContext.disableScissor()

        val useAnimation = usingMagicList[index] ?: return
        val maxX = xIndent + 55 + extraWidthAnimation.animatedValue + offsetX
        val startX = xIndent + useAnimation + offsetX
        val endX = startX + rowHeight
        if (startX > maxX) {
            usingMagicList.remove(index)
            return
        }

        val animationProgress = ((startX - xIndent - 50.0) / extraWidth).coerceIn(.0, 1.0)
        drawContext.enableScissor(left, top, right, bottom)
        drawSlantedBand(
            drawContext,
            startX,
            top,
            rowHeight,
            rowHeight,
            rowHeight,
            color((alpha * 255 * (1.0 - animationProgress)).roundToInt(), 0, 255, 0),
        )
        drawSlantedBand(
            drawContext,
            startX,
            top,
            rowHeight,
            rowHeight,
            rowHeight,
            color((alpha * 128).roundToInt(), 25, 192, 25),
        )
        drawSlantedBand(
            drawContext,
            startX + 10.0,
            top,
            rowHeight,
            rowHeight,
            rowHeight,
            color((alpha * 255).roundToInt(), 25, 255, 25),
        )
        drawSlantedBand(
            drawContext,
            startX + 20.0,
            top,
            rowHeight,
            rowHeight,
            rowHeight,
            color((alpha * 128).roundToInt(), 25, 192, 25),
        )
        drawContext.disableScissor()
    }

    private fun renderRightPart(drawContext: GuiGraphicsExtractor) {
        val alpha = magicShownOpacityAnimation.animatedValue
        val width = drawContext.guiWidth()
        val height = drawContext.guiHeight()
        val offsetX = magicShownAnimation.animatedValue
        val offsetY = descriptionExtraHeightAnimation.animatedValue
        val left = (width - 200 - offsetX).roundToInt()
        val right = (width - 25 - offsetX).roundToInt()
        val top = (height / 2.0 - 100.0 - offsetY / 2.0).roundToInt()
        val bottom = (height / 2.0 + 100.0 + offsetY / 2.0).roundToInt()

        drawContext.fill(left, top, right, bottom, color((alpha * 127.5).roundToInt(), 255, 255, 255))

        val target = cachedTargetedEntity
        val descriptionAlpha = min(alpha, entityDescriptionOpacityAnimation.animatedValue)
        if (target != null) {
            renderTargetDescription(drawContext, target, descriptionAlpha, offsetX, offsetY)
        }

        if (alpha <= 0.016) {
            return
        }

        val selectedMagic = selectedMagicOrNull() ?: return
        if (selectedMagic.definition.description != currentDescription) {
            currentDescription = selectedMagic.definition.description
            displayDescription = currentDescription
            magicDescriptionChangedAnimation.animatedValue = .0
            magicDescriptionChangedAnimation.value = 1.0
        }

        val lines = minecraft.font.split(currentDescription, 150)
        val extraHeight = minecraft.font.lineHeight * lines.size - 180.0
        descriptionExtraHeightAnimation.value = extraHeight.coerceAtLeast(.0)

        drawContext.enableScissor(left, top, right, bottom)
        val descriptionY = (height / 2.0 - 55.0 + descriptionYOffsetAnimation.animatedValue - descriptionExtraHeightAnimation.animatedValue / 2.0).roundToInt()
        val textAlpha = min(alpha, magicDescriptionChangedAnimation.animatedValue)
        if (textAlpha > 0.016) {
            drawContext.textWithWordWrap(
                minecraft.font,
                displayDescription,
                width - 190 - offsetX.roundToInt(),
                descriptionY,
                150,
                color((textAlpha * 255).roundToInt(), 255, 255, 255),
            )
        }
        drawContext.disableScissor()
    }

    private fun renderTargetDescription(
        drawContext: GuiGraphicsExtractor,
        target: LivingEntity,
        descriptionAlpha: Double,
        offsetX: Double,
        offsetY: Double,
    ) {
        if (descriptionAlpha <= 0.016) {
            return
        }

        val width = drawContext.guiWidth()
        val height = drawContext.guiHeight()
        val health = target.health.toDouble()
        val maxHealth = target.maxHealth.toDouble()
        if (health.isFinite()) {
            healthAnimation.value = health
        }
        if (maxHealth.isFinite()) {
            maxHealthAnimation.value = maxHealth
        }
        healthPercentageAnimation.value = if (maxHealth > .0) (health / maxHealth).coerceIn(.0, 1.0) else .0

        val percentage = healthPercentageAnimation.animatedValue
        val barLeft = width - 190 - offsetX.roundToInt()
        val barRight = (width - lerp(percentage, 190.0, 35.0) - offsetX).roundToInt()
        val barTop = (height / 2.0 - 80.0 - offsetY / 2.0).roundToInt()
        val barBottom = (height / 2.0 - 75.0 - offsetY / 2.0).roundToInt()
        val red = (lerp(percentage, 255.0, 25.0)).roundToInt()
        val green = (lerp(percentage, 25.0, 255.0)).roundToInt()
        drawContext.fill(barLeft.coerceAtMost(barRight), barTop, barRight, barBottom, color((descriptionAlpha * 255).roundToInt(), red, green, 25))

        val textColor = color((descriptionAlpha * 255).roundToInt(), 255, 255, 255)
        val entityNameKey = target.displayName.string
        if (entityNameKey != previousDisplayEntityNameKey) {
            previousDisplayEntityNameKey = entityNameKey
            displayEntityName = target.displayName
            displayEntityNameOpacityAnimation.animatedValue = .0
            displayEntityNameOpacityAnimation.value = 1.0
        }

        val nameAlpha = min(descriptionAlpha, displayEntityNameOpacityAnimation.animatedValue)
        if (nameAlpha > 0.016) {
            drawContext.text(
                minecraft.font,
                displayEntityName,
                barLeft,
                (height / 2.0 - 90.0 - offsetY / 2.0).roundToInt(),
                color((nameAlpha * 255).roundToInt(), 255, 255, 255),
            )
        }

        if (candidateTargets.size != previousCandidateEntityCount) {
            previousCandidateEntityCount = candidateTargets.size
            displayCandidateEntityCount = candidateTargets.size
            displayCandidateCountOpacityAnimation.animatedValue = .0
            displayCandidateCountOpacityAnimation.value = 1.0
        }

        val candidateAlpha = min(descriptionAlpha, displayCandidateCountOpacityAnimation.animatedValue)
        if (candidateAlpha > 0.016) {
            candidateCountPaddingAnimation.value = (minecraft.font.width(displayEntityName) + minecraft.font.width(" ")).toDouble()
            drawContext.text(
                minecraft.font,
                "+$displayCandidateEntityCount",
                (barLeft + candidateCountPaddingAnimation.animatedValue).roundToInt(),
                (height / 2.0 - 90.0 - offsetY / 2.0).roundToInt(),
                color((candidateAlpha * 255).roundToInt(), 255, 255, 255),
            )
        }

        val healthText = "${formatDecimal(healthAnimation.animatedValue, scale = 2)}/${formatDecimal(maxHealthAnimation.animatedValue, scale = 2)}"
        drawContext.text(
            minecraft.font,
            healthText,
            barLeft,
            (height / 2.0 - 70.0 - offsetY / 2.0).roundToInt(),
            textColor,
        )
    }

    private fun renderManaCostTooltip(drawContext: GuiGraphicsExtractor) {
        val magic = selectedMagicOrNull() ?: return
        val calculationContext = MagicCalculationContext.fromEntity(minecraft.player, targetedEntity)
        val normalCost = runCatching { magic.getNormalCost() }.getOrDefault(magic.definition.baseCost.toDouble().toLong())
        val cost = runCatching { magic.getCost(calculationContext) }.getOrDefault(normalCost)

        if (cost == normalCost || magicShownOpacityAnimation.animatedValue <= 0.001) {
            manaCostTooltipShownAnimation.value = .0
            manaCostTooltipOpacityAnimation.value = .0
        } else {
            manaCostTooltipShownAnimation.value = 70.0
            manaCostTooltipOpacityAnimation.value = 1.0
            if (cost > normalCost) {
                manaCostTooltipRedAnimation.value = .5
                manaCostTooltipGreenAnimation.value = .0
                manaCostTooltipBlueAnimation.value = .0
            } else {
                manaCostTooltipRedAnimation.value = .0
                manaCostTooltipGreenAnimation.value = .5
                manaCostTooltipBlueAnimation.value = .0
            }
        }

        val opacity = manaCostTooltipOpacityAnimation.animatedValue
        if (opacity <= 0.001) {
            return
        }

        val left = drawContext.guiWidth() / 2 - 125
        val top = (drawContext.guiHeight() - manaCostTooltipShownAnimation.animatedValue).roundToInt()
        val right = drawContext.guiWidth() / 2 + 125
        val bottom = top + 15
        drawContext.fill(
            left,
            top,
            right,
            bottom,
            color(
                (opacity * 128).roundToInt(),
                (manaCostTooltipRedAnimation.animatedValue * 255).roundToInt(),
                (manaCostTooltipGreenAnimation.animatedValue * 255).roundToInt(),
                (manaCostTooltipBlueAnimation.animatedValue * 255).roundToInt(),
            ),
        )

        val difference = abs(normalCost - cost)
        if (difference != lastManaCostTooltipDifference) {
            lastManaCostTooltipDifference = difference
            displayManaCostTooltipDifference = difference
            manaCostTooltipDifferenceChangedAnimation.animatedValue = .0
            manaCostTooltipDifferenceChangedAnimation.value = 1.0
        }
        val state = cost > normalCost
        if (state != lastManaCostTooltipState) {
            lastManaCostTooltipState = state
            displayManaCostTooltipState = state
            manaCostTooltipStateChangedAnimation.animatedValue = .0
            manaCostTooltipStateChangedAnimation.value = 1.0
        }

        val stateAlpha = min(opacity, manaCostTooltipStateChangedAnimation.animatedValue)
        if (stateAlpha > 0.016) {
            drawContext.text(
                minecraft.font,
                if (displayManaCostTooltipState) MatrixLanguage.manaCostIncreased else MatrixLanguage.manaCostReduced,
                left + 5,
                top + 3,
                color((stateAlpha * 255).roundToInt(), 255, 255, 255),
            )
        }
        val differenceAlpha = min(opacity, manaCostTooltipDifferenceChangedAnimation.animatedValue)
        if (differenceAlpha > 0.016) {
            val differenceText = displayManaCostTooltipDifference.toString()
            drawContext.text(
                minecraft.font,
                differenceText,
                right - 5 - minecraft.font.width(differenceText),
                top + 3,
                color((differenceAlpha * 255).roundToInt(), 255, 255, 255),
            )
        }
    }

    private fun renderMagicAvailableStatus(drawContext: GuiGraphicsExtractor, hudActive: Boolean) {
        val magic = selectedMagicOrNull() ?: return
        val status = getMagicStatusDisplay(magic)
        if (!hudActive || status.available || status.targetMissing) {
            availableStatusShownAnimation.value = -50.0
            availableStatusOpacityAnimation.value = .0
        } else {
            availableStatusShownAnimation.value = .0
            availableStatusOpacityAnimation.value = 1.0
        }

        if (status.key != currentAvailableStatusKey && !status.available) {
            currentAvailableStatusKey = status.key
            displayAvailableStatus = status.text
            availableStatusChangedAnimation.animatedValue = .0
            availableStatusChangedAnimation.value = 1.0
        }

        val opacity = availableStatusOpacityAnimation.animatedValue
        if (opacity <= 0.001) {
            return
        }

        val left = drawContext.guiWidth() / 2 - 125
        val top = (30.0 + availableStatusShownAnimation.animatedValue).roundToInt()
        val right = drawContext.guiWidth() / 2 + 125
        val bottom = (45.0 + availableStatusShownAnimation.animatedValue).roundToInt()
        drawContext.fill(left, top, right, bottom, color((opacity * 128).roundToInt(), 128, 0, 0))

        val textAlpha = min(opacity, availableStatusChangedAnimation.animatedValue)
        if (textAlpha > 0.016) {
            drawContext.text(
                minecraft.font,
                displayAvailableStatus,
                left + 5,
                top + 3,
                color((textAlpha * 255).roundToInt(), 255, 255, 255),
            )
        }
    }

    private fun renderOverclock(drawContext: GuiGraphicsExtractor) {
        val alpha = magicShownOpacityAnimation.animatedValue
        if (alpha <= 0.001) {
            return
        }
        manaOverclockAnimation.value = manaOverclock
        magicOverclockAnimation.value = magicOverclock

        val width = drawContext.guiWidth()
        val height = drawContext.guiHeight()
        val manaTop = lerp((manaOverclockAnimation.animatedValue / 10.0).coerceIn(.0, 1.0), height - 25.0, 25.0).roundToInt()
        val magicTop = lerp((magicOverclockAnimation.animatedValue / 10.0).coerceIn(.0, 1.0), height - 25.0, 25.0).roundToInt()
        val manaColor = overclockColor(manaOverclockAnimation.animatedValue, alpha)
        val magicColor = overclockColor(magicOverclockAnimation.animatedValue, alpha)

        drawContext.fill(width - 10, manaTop, width - 5, height - 25, manaColor)
        drawContext.fill(width - 20, magicTop, width - 15, height - 25, magicColor)
    }

    private fun checkMagicAvailableStatus() {
        val magics = currentMagicList()
        fillColorAnimationList(magics)

        magics.forEachIndexed { index, magic ->
            val color = magicColorAnimations[index]
            if (index == selectedMagicIndex) {
                if (color.green.value == 255.0 || color.green.value == 128.0) {
                    color.setColor(.0, 128.0, .0)
                } else {
                    color.setColorWithoutAnimation(.0, 128.0, .0)
                }
                return@forEachIndexed
            }

            val status = getMagicStatusDisplay(magic)
            if (status.available || status.targetMissing) {
                color.setColor(.0, .0, .0)
            } else {
                color.setColor(128.0, .0, .0)
            }
        }
    }

    private fun performChannelMagicAnimation() {
        val now = System.nanoTime()
        val delta = now - lastFrameNanos
        usingMagicList.keys.toList().forEach { index ->
            usingMagicList[index] = (usingMagicList[index] ?: .0) + delta / 2_000_000.0
        }
    }

    private val magicDisplayData: MutableList<MagicDisplayData>
        get() = cachedMagicDisplayData

    private fun ensureMagicDisplayData(magics: List<Magic>) {
        if (cachedMagicDisplayData.size == magics.size) {
            return
        }
        cachedMagicDisplayData = MutableList(magics.size) { MagicDisplayData() }
    }

    private fun fillColorAnimationList(magics: List<Magic>) {
        while (magicColorAnimations.size < magics.size) {
            magicColorAnimations.add(HudColorAnimation())
        }
    }

    private fun fillExtraWidthAnimationList(magics: List<Magic>) {
        while (magicExtraWidthAnimations.size < magics.size) {
            magicExtraWidthAnimations.add(SimpleDoubleAnimation(initValue = .0))
        }
    }

    private fun getMagicStatusDisplay(
        magic: Magic,
        targetMissingAsAvailable: Boolean = false,
    ): MagicStatusDisplay {
        val status = getMagicAvailability(magic).firstOrNull()
        if (status == null) {
            return MagicStatusDisplay("available", MatrixLanguage.magicAvailable, available = true, targetMissing = false)
        }

        val targetMissing = status.identifier.path == MagicAvailableStatus.TargetMissing.identifier.path
        if (targetMissingAsAvailable && targetMissing) {
            return MagicStatusDisplay("available", MatrixLanguage.magicAvailable, available = true, targetMissing = true)
        }

        return MagicStatusDisplay(
            key = status.identifier.toString(),
            text = legacyStatusText(status),
            available = false,
            targetMissing = targetMissing,
        )
    }

    private fun getMagicAvailability(magic: Magic): MagicAvailability {
        val context = MagicCalculationContext.fromEntity(minecraft.player, targetedEntity)
        return runCatching { magic.availableStatus(context) }
            .getOrDefault(MagicAvailability(MagicAvailableStatus.Unavailable))
    }

    private fun legacyStatusText(status: MagicAvailableStatus): Component {
        return when (status.identifier.path) {
            MagicAvailableStatus.InsufficientMana.identifier.path -> MatrixLanguage.magicAvailableManaNotEnough
            MagicAvailableStatus.TargetImmune.identifier.path -> MatrixLanguage.magicTargetImmune
            MagicAvailableStatus.Unavailable.identifier.path -> MatrixLanguage.magicUnavailable
            MagicAvailableStatus.ChannelQueueFull.identifier.path -> MatrixLanguage.magicChannelQueueFull
            MagicAvailableStatus.ChannelQueueLocked.identifier.path -> MatrixLanguage.magicChannelQueueLocked
            MagicAvailableStatus.TargetMissing.identifier.path -> MatrixLanguage.magicTargetMissing
            "sculk_catalyst_is_already_active" -> MatrixLanguage.magicSculkCatalystIsAlreadyActive
            else -> status.description
        }
    }

    private fun generateIndentList(size: Int): List<Double> {
        if (size <= 0) {
            return emptyList()
        }
        val ease = ElasticEase().also {
            it.oscillations = 0
            it.springiness = 1.0
            it.easingMode = EasingMode.OUT
        }
        val center = size / 2.0
        return List(size) { index ->
            val distance = abs(center - index)
            val current = 1.0 - ease.transform(1.0 - (distance / center).coerceIn(.0, 1.0))
            current * 50.0 - 25.0
        }
    }

    private fun drawSlantedBand(
        drawContext: GuiGraphicsExtractor,
        startX: Double,
        top: Int,
        width: Double,
        height: Double,
        slant: Double,
        color: Int,
    ) {
        val rows = height.roundToInt().coerceAtLeast(1)
        for (row in 0 until rows) {
            val t = row / rows.toDouble()
            val left = startX + slant * (1.0 - t)
            val right = left + width
            drawContext.fill(left.roundToInt(), top + row, right.roundToInt().coerceAtLeast(left.roundToInt() + 1), top + row + 1, color)
        }
    }

    private fun overclockColor(value: Double, alpha: Double): Int {
        val ratio = (value / 10.0).coerceIn(.0, 1.0)
        return color(
            (alpha * 128).roundToInt(),
            lerp(ratio, .0, 255.0).roundToInt(),
            lerp(ratio, 255.0, .0).roundToInt(),
            0,
        )
    }

    private fun formatDecimal(value: Double, scale: Int = 1): String {
        if (!value.isFinite()) {
            return "∞"
        }
        return BigDecimal.valueOf(value)
            .setScale(scale, RoundingMode.HALF_UP)
            .toPlainString()
    }

    private fun lerp(delta: Double, from: Double, to: Double): Double {
        return from + (to - from) * delta
    }

    private fun selectedMagicOrNull(): Magic? {
        val magics = currentMagicList()
        selectedMagicIndex = selectedMagicIndex.coerceIn(0, (magics.size - 1).coerceAtLeast(0))
        return magics.getOrNull(selectedMagicIndex)
    }

    private fun bestUsableTarget(magic: Magic): LivingEntity? {
        val target = targetedEntity
        if (target != null && canChannelMagicOn(target, magic)) {
            return target
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

    private fun Vec3.toGuiScreenPosition(
        tickDelta: Float,
        viewportWidth: Int,
        viewportHeight: Int,
        centered: Boolean = false,
    ): Vector2f? {
        val screenPosition = worldToScreen(
            this,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
        ) ?: return null

        val halfSize = if (centered) 0.0 else 7.5
        val maxX = viewportWidth - if (centered) 0.0 else 15.0
        val maxY = viewportHeight - if (centered) 0.0 else 15.0
        return Vector2f(
            (screenPosition.x - halfSize).coerceIn(0.0, maxX).toFloat(),
            (screenPosition.y - halfSize).coerceIn(0.0, maxY).toFloat(),
        )
    }

    private fun LivingEntity.guidePosition(tickDelta: Float): Vec3 {
        return getLerpedPos(tickDelta).add(.0, boundingBox.ysize * 0.5, .0)
    }

    private fun Entity?.asLivingTarget(): LivingEntity? {
        return when (this) {
            is LivingEntity -> this
            is EnderDragonPart -> parentMob
            else -> null
        }
    }

    private fun shouldRenderMagicList(): Boolean {
        val player = minecraft.player ?: return false
        return player.isWizard &&
                minecraft.gui.screen() == null &&
                currentMagicList().isNotEmpty() &&
                magicListKeyDown
    }

    private fun hasHudContext(): Boolean {
        val player = minecraft.player ?: return false
        return player.isWizard && currentMagicList().isNotEmpty()
    }

    private fun Int.floorMod(modulo: Int): Int {
        return ((this % modulo) + modulo) % modulo
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
