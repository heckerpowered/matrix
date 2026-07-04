/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import com.mojang.blaze3d.pipeline.BlendFunction
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.core.AimAssist
import heckerpowered.matrix.client.core.ClientOptions.aimAssistEnabled
import heckerpowered.matrix.client.core.ClientOptions.aimAssistFov
import heckerpowered.matrix.client.core.ClientOptions.aimAssistMaxDistance
import heckerpowered.matrix.client.event.KeyEvent
import heckerpowered.matrix.client.event.MouseButtonEvent
import heckerpowered.matrix.client.render.*
import heckerpowered.matrix.client.render.post.BloomEffect
import heckerpowered.matrix.client.render.shader.GaussianBlurRenderer
import heckerpowered.matrix.client.render.shader.OpacityMaskRenderer
import heckerpowered.matrix.client.shader.*
import heckerpowered.matrix.client.ui.element.*
import heckerpowered.matrix.client.ui.foundation.animation.*
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.item.LightningChestplate1
import heckerpowered.matrix.common.item.LightningChestplate1.isBorrowedTime
import heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking
import heckerpowered.matrix.common.item.WizardHelmet5
import heckerpowered.matrix.common.magic.core.LMagicAvailableStatus
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicAvailability
import heckerpowered.matrix.common.magic.core.MagicAvailableStatus
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.core.description
import heckerpowered.matrix.common.network.ServerboundActivateBloodPactPayload
import heckerpowered.matrix.common.network.ServerboundBorrowedTimePayload
import heckerpowered.matrix.client.render.gui.DissolveRect
import heckerpowered.matrix.client.render.gui.DissolveRectRenderState
import heckerpowered.matrix.extension.MatrixGuiRenderState
import heckerpowered.matrix.common.network.ServerboundOverclockPayload
import heckerpowered.matrix.common.network.ServerboundUseMagicPayload
import heckerpowered.matrix.common.persistent.isWizard
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import heckerpowered.matrix.core.approximatelyEqual
import heckerpowered.matrix.core.inverseLerp
import heckerpowered.matrix.core.lerp
import heckerpowered.matrix.core.utility.getEntitiesNearSight
import heckerpowered.matrix.core.worldToScreen
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB
import net.minecraft.util.ARGB
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.ClipContext
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.random.Random

object MatrixHud {
    var mana
        get() = manaBar.mana.value
        set(value) {
            manaBar.mana.value = value.coerceIn(0.0..maxMana)
        }

    var maxMana
        get() = manaBar.maxMana.value.coerceAtLeast(.0)
        set(value) {
            manaBar.maxMana.value = value
        }

    var manaUsage
        get() = manaBar.manaUsage.value
        set(value) {
            manaBar.manaUsage.value = value
        }

    var targetedEntity: LivingEntity? = null
    private var usingRayCast = false
    private var cachedTargetedEntity: LivingEntity? = null

    private var lastNanos = 0L

    private var selectedIndex = 1
    private var usingMagicList = mutableMapOf<Int, Double>()

    private var previousIndex = 1

    private val manaBar = ManaBar()

    private val easingFunction = ElasticEase()

    private val manaOverclockAnimation = AnimationClock(Duration.ofMillis(300), 1.0, 1.0)
    private val magicOverclockAnimation = AnimationClock(Duration.ofMillis(300), 1.0, 1.0)

    private val magicShownAnimationClock = AnimationClock(
        Duration.ofMillis(
            300
        ), -50.0, .0
    )
    private val magicShownOpacityAnimationClock = AnimationClock(
        Duration.ofMillis(
            300
        ), .0, 1.0
    )
    private val magicShownAnimation = DoubleAnimation(
        magicShownAnimationClock, easingFunction
    )
    val magicShownOpacityAnimation = DoubleAnimation(
        magicShownOpacityAnimationClock, easingFunction
    )

    private val hudBloomThreshold = SimpleDoubleAnimation(from = 1.0, to = 1.0)

    private val magicTimeScale = TimeController.allocateTimeController()
    private val lightningTimeScale = TimeController.allocateTimeController()

    private val healthPercentageAnimation = SimpleDoubleAnimation()
    private val healthAnimation = SimpleDoubleAnimation()
    private val maxHealthAnimation = SimpleDoubleAnimation()
    private var previousDisplayEntityNameHashCode: Int = 0
    private var displayEntityName: Component = Component.empty()
    private var displayEntityNameOpacityAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(150), initValue = 1.0)
    private var previousCandidateEntities: Int = 0
    private var displayCandidateEntities: Int = 0
    private var displayCandidateCountOpacityAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(150), initValue = 1.0)
    private var candidateCountPadding = SimpleDoubleAnimation()

    private val manaOverclock = DoubleAnimation(
        manaOverclockAnimation, easingFunction
    )
    private val magicOverclock = DoubleAnimation(
        magicOverclockAnimation, easingFunction
    )

    private val dissolveShader = DissolveShader()

    private val dissolveAnimation = SimpleDoubleAnimation(from = 1.0)

    private val magicDescriptionChangedAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(150))
    private var currentDescription: Component = Component.empty()
    private var displayDescription: Component = Component.empty()

    private val descriptionYOffsetAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(300))

    private val entityDescriptionOpacityAnimation = SimpleDoubleAnimation(initValue = 1.0)
    private var previousVisibility = false

    private var aimEntity: Entity? = null
    private var useAimAssist = false
    private var firstShow = true

    private val grayscaleIntensityAnimation = SimpleDoubleAnimation()
    private val descriptionExtraHeightAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(450))

    private var sizeScalingAnimation = true

    @JvmField
    val fovAnimation = SimpleDoubleAnimation(initValue = 1.0)

    private var fovZoomRatio = .0
    // 26.2: no longer private -- StatusHud's progress-ring pass previously drew into this target
    // implicitly (it was the bound framebuffer during onHudRender); explicit targets are now
    // required, so HUD elements that composite through the blur/bloom chain reference it.
    @JvmStatic
    val hudFramebuffer by lazy { PostProcessRenderer.createManagedFramebuffer() }
    private val blurFramebuffer by lazy { PostProcessRenderer.createManagedFramebuffer() }
    private val emissiveFramebuffer by lazy { PostProcessRenderer.createManagedFramebuffer() }

    private class MagicDisplayData {
        val statusChangedAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(150))
        val costChangedAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(150))
        val costWidthAnimation = SimpleDoubleAnimation()

        var displayCost = 0L
        var previousCost = 0L
        var displayStatus = LMagicAvailableStatus.AVAILABLE
        var previousStatus = LMagicAvailableStatus.AVAILABLE
    }

    private var cachedMagicDisplayData = mutableListOf<MagicDisplayData>()
    private val magicDisplayData: MutableList<MagicDisplayData>
        get() {
            val magics = MatrixClient.getPlayerMagics()
            if (cachedMagicDisplayData.size != magics.size) {
                val newCachedMagicDisplayData = mutableListOf<MagicDisplayData>()
                for (i in magics.indices) {
                    newCachedMagicDisplayData.add(MagicDisplayData())
                }
                cachedMagicDisplayData = newCachedMagicDisplayData
            }
            return cachedMagicDisplayData
        }

    // 26.2: post/the_world.fsh is std140-converted -- `layout(std140) uniform MatrixPostUniforms
    // { vec4 MatrixPostData0..3; }` with `#define grayscaleIntensity MatrixPostData0.x`, plus a
    // `framebuffer` sampler bound to the previous pass output.
    private val theWorldShader by lazy {
        BlitProgram(
            "post/the_world.fsh",
            uniforms = arrayOf(
                UniformProvider("MatrixPostUniforms") {
                    putVec4(grayscaleIntensityAnimation.animatedValue.toFloat(), 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                    putVec4(0F, 0F, 0F, 0F)
                }
            ),
            textures = arrayOf(PostProcessRenderer.framebufferProvider)
        )
    }

    // TODO(26.2): the point-sprite program rendered GL_POINTS with GL_PROGRAM_POINT_SIZE through
    // a transform-feedback component (see renderPoints below). The dual-backend wrapper API has
    // no transform feedback, no point-sprite primitive, and no client vertex-array path, so this
    // debug-only effect (never invoked from any live code path) cannot be ported as-is. Kept for
    // reference:
    // private val pointSpriteProgram by lazy {
    //     Program(
    //         ResourceShader("/assets/matrix/shaders/point_sprite/point_sprite.vsh", GL_VERTEX_SHADER),
    //         ResourceShader("/assets/matrix/shaders/point_sprite/point_sprite.fsh", GL_FRAGMENT_SHADER),
    //         uniforms = arrayOf(UniformProvider("time") { pointer ->
    //             val timeSeconds = System.nanoTime() / 1_000_000_000.0
    //             glUniform1f(pointer, timeSeconds.toFloat())
    //         }),
    //         components = arrayOf(
    //             TransformFeedback(
    //                 arrayOf("OutPosition"),
    //                 (points.size / 2),
    //                 bufferSize = (points.size / 2).toLong() * 4 * 2
    //             )
    //         )
    //     )
    // }

    init {
        easingFunction.easingMode = EasingMode.OUT
        easingFunction.oscillations = 0

        magicOverclock.currentValue = 1.0
        manaOverclock.currentValue = 1.0

        entityDescriptionOpacityAnimation.start()
        descriptionYOffsetAnimation.start()
        grayscaleIntensityAnimation.start()
        descriptionExtraHeightAnimation.start()
    }

    private class ColorAnimation {
        val redClock = AnimationClock(
            Duration.ofMillis(
                300
            ), .0, 1.0
        )
        val red = DoubleAnimation(
            redClock, easingFunction
        )

        val greenClock = AnimationClock(
            Duration.ofMillis(
                300
            ), .0, 1.0
        )
        val green = DoubleAnimation(
            greenClock, easingFunction
        )

        val blueClock = AnimationClock(
            Duration.ofMillis(
                300
            ), .0, 1.0
        )
        val blue = DoubleAnimation(
            blueClock, easingFunction
        )

        fun setColor(
            red: Double,
            green: Double,
            blue: Double,
        ) {
            this.red.currentValue = red
            this.green.currentValue = green
            this.blue.currentValue = blue
        }

        fun setColorWithoutAnimation(
            red: Double,
            green: Double,
            blue: Double,
        ) {
            redClock.from = red
            redClock.to = red
            this.red.currentValue = red

            greenClock.from = green
            greenClock.to = green
            this.green.currentValue = green

            blueClock.from = blue
            blueClock.to = blue
            this.blue.currentValue = blue
        }
    }

    private val magicColorAnimations = mutableListOf<ColorAnimation>()
    private val magicExtraWidthAnimations = mutableListOf<PackedAnimation>()

    private var candidateAimAssistEntities = emptyList<LivingEntity>()

    @JvmStatic
    fun onDoAttack() {
        if (!shouldRenderHud() || shouldSlowTime()) {
            useAimAssist = false
            return
        }
        useAimAssist = !useAimAssist
        if (!useAimAssist) {
            aimEntity = null
        }
    }

    private fun processKeyInput() {
        if (MatrixKeyBindings.useMagic.consumeClick()) {
            useCurrentMagic()
        }
        while (MatrixKeyBindings.useMagic.consumeClick()) {
            useCurrentMagic()
        }

        while (MatrixKeyBindings.nextMagic.consumeClick()) {
            nextMagic()
        }

        while (MatrixKeyBindings.previousMagic.consumeClick()) {
            previousMagic()
        }

        while (MatrixKeyBindings.overclockMagic.consumeClick()) {
            val difference = if (player.isShiftKeyDown) {
                -0.5
            } else {
                0.5
            }
            val newOverclock = (magicOverclock.currentValue + difference).coerceIn(
                1.0..10.0
            )
            magicOverclock.currentValue = newOverclock
            ClientPlayNetworking.send(ServerboundOverclockPayload(manaOverclock.currentValue, magicOverclock.currentValue))
        }

        while (MatrixKeyBindings.overclockMana.consumeClick()) {
            val difference = if (player.isShiftKeyDown) {
                -0.5
            } else {
                0.5
            }
            val newOverclock = (manaOverclock.currentValue + difference).coerceIn(
                1.0..10.0
            )
            manaOverclock.currentValue = newOverclock
            ClientPlayNetworking.send(
                ServerboundOverclockPayload(
                    manaOverclock.currentValue, magicOverclock.currentValue
                )
            )
        }
    }

    fun onInitialize() {
        // 26.2: HudRenderCallback was replaced by HudElementRegistry; still invoked once per rendered frame.
        HudElementRegistry.addLast(Matrix.identifier("matrix_hud")) { drawContext, tickCounter ->
            onHudRender(drawContext, tickCounter)
        }
        SystemCrashBar.onInitialize()
        StatusHud.onInitialize()
        DamageNumberHud.onInitialize()
    }

    @JvmStatic
    fun overclockMana() {
        val newOverclock = (manaOverclock.currentValue + 0.5).coerceIn(
            1.0..10.0
        )
        manaOverclock.currentValue = newOverclock
        // 26.2: see the ServerboundOverclockPayload comment in processKeyInput.
        ClientPlayNetworking.send(
            ServerboundOverclockPayload(
                manaOverclock.currentValue, magicOverclock.currentValue
            )
        )
    }

    @JvmStatic
    fun underclockMana() {
        val newOverclock = (manaOverclock.currentValue - 0.5).coerceIn(
            1.0..10.0
        )
        manaOverclock.currentValue = newOverclock
        // 26.2: see the ServerboundOverclockPayload comment in processKeyInput.
        ClientPlayNetworking.send(
            ServerboundOverclockPayload(
                manaOverclock.currentValue, magicOverclock.currentValue
            )
        )
    }

    @JvmStatic
    fun overclockMagic() {
        val newOverclock = (magicOverclock.currentValue + 0.5).coerceIn(
            1.0..10.0
        )
        magicOverclock.currentValue = newOverclock
        // 26.2: see the ServerboundOverclockPayload comment in processKeyInput.
        ClientPlayNetworking.send(
            ServerboundOverclockPayload(
                manaOverclock.currentValue, magicOverclock.currentValue
            )
        )
    }

    @JvmStatic
    fun underclockMagic() {
        val newOverclock = (magicOverclock.currentValue - 0.5).coerceIn(
            1.0..10.0
        )
        magicOverclock.currentValue = newOverclock
        // 26.2: see the ServerboundOverclockPayload comment in processKeyInput.
        ClientPlayNetworking.send(
            ServerboundOverclockPayload(
                manaOverclock.currentValue, magicOverclock.currentValue
            )
        )
    }

    @JvmStatic
    fun nextMagic() {
        previousIndex = selectedIndex
        ++selectedIndex
        if (selectedIndex > MatrixClient.getPlayerMagics().size) {
            selectedIndex = 1
        }
        onSelectionChanged()
    }

    @JvmStatic
    fun previousMagic() {
        previousIndex = selectedIndex
        --selectedIndex
        if (selectedIndex < 1) {
            selectedIndex = MatrixClient.getPlayerMagics().size
        }
        onSelectionChanged()
    }


    private fun onSelectionChanged() {
        if (selectedIndex - 1 !in MatrixClient.getPlayerMagics().indices) {
            return
        }

        val magic = selectedMagic
        val calculationContext = MagicCalculationContext.fromEntity(player, targetedEntity)
        manaBar.manaCost.value = magic.getCost(calculationContext).toDouble()
    }

    val selectedMagic: Magic
        get() {
            var index = selectedIndex - 1
            val magics = MatrixClient.getPlayerMagics()
            if (index !in magics.indices) {
                index = 0
            }

            return magics[index]
        }

    private fun useCurrentMagic() {
        if (!shouldRenderHud()) {
            return
        }

        useMagicIndexed(selectedIndex - 1)
    }

    private fun useMagicIndexed(index: Int) {
        val magic = MatrixClient.getPlayerMagics()[index]
        val target = this.targetedEntity ?: return
        val calculationContext = MagicCalculationContext.fromEntity(player, target)
        if (!magic.availableStatus(calculationContext).isAvailable) {
            return
        }

        magicColorAnimations[index].setColorWithoutAnimation(.0, 255.0, .0)

        // Reset use magic animation
        usingMagicList[index + 1] = 0.0

        val cost = magic.getCost(calculationContext)
        val mana = mana - manaUsage
        if (player.isBloodPactActive && mana < cost) {
            minecraft.level!!.playSound(player, player.x, player.y, player.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1f)
            minecraft.level!!.playSound(player, player.x, player.y, player.z, SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0f, 1f)
        } else {
            minecraft.level!!.playSound(player, player.x, player.y, player.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1f)
        }

        channelMagic(magic, target)
    }

    private fun channelMagic(magic: Magic, target: LivingEntity) {
        ClientPlayNetworking.send(ServerboundUseMagicPayload(magic.definition.uuid, target.id))
    }

    private fun checkVisibilityChanges() {
        val shouldRenderHud = shouldRenderHud()
        if (shouldRenderHud != previousVisibility) {
            onHudVisibilityChanged(shouldRenderHud)
        }
        previousVisibility = shouldRenderHud
    }

    private fun warp() {
        if (TimeController.minTimeScale != 1.0 || player.isPhaseWalking) {
            grayscaleIntensityAnimation.value = 1.0
        } else {
            grayscaleIntensityAnimation.value = .0
        }

        if (!shouldSlowTime()) {
            return
        }
        val hasLightningEffect = player.isBorrowedTime
        TimeController.playerStandaloneRenderTick = hasLightningEffect && !shouldRenderHud() && !minecraft.isPaused
        if (hasLightningEffect) {
            lightningTimeScale.value = 0.15
        } else {
            lightningTimeScale.value = 1.0
        }
        TimeController.onRenderTick()
        PostProcessRenderer.postProcessShaders.add(theWorldShader)
    }

    private fun checkMagicAvailableStatus() {
        val magics = MatrixClient.getPlayerMagics()
        fillColorAnimationList(magics)

        magics.forEachIndexed { index, magic ->
            if (index == selectedIndex - 1) {
                val color = magicColorAnimations[index]
                // when green = 255, magic is used, perform animation
                // when green = 128, animation is in progress
                if (color.green.currentValue == 255.0 || color.green.currentValue == 128.0) {
                    color.setColor(
                        .0, 128.0, .0
                    )
                } else {
                    color.setColorWithoutAnimation(
                        .0, 128.0, .0
                    )
                }

                return@forEachIndexed
            }

            val status = getMagicAvailableStatus(
                magic
            )
            when (status) {
                LMagicAvailableStatus.TARGET_MISSING -> magicColorAnimations[index].setColor(
                    .0, .0, .0
                )

                LMagicAvailableStatus.AVAILABLE -> magicColorAnimations[index].setColor(
                    .0, .0, .0
                )

                else -> {
                    magicColorAnimations[index].setColor(128.0, 0.0, 0.0)
                }
            }
        }
    }

    private fun fillColorAnimationList(
        magics: List<Magic>,
    ) {
        if (magicColorAnimations.size >= magics.size) {
            return
        }

        for (i in magicColorAnimations.size..magics.size) {
            magicColorAnimations.add(
                ColorAnimation()
            )
        }
    }

    private fun fillExtraWidthAnimationList(
        magics: List<Magic>,
    ) {
        if (magicExtraWidthAnimations.size >= magics.size) {
            return
        }

        for (i in magicExtraWidthAnimations.size..magics.size) {
            val animationClock = AnimationClock(
                Duration.ofMillis(
                    300
                ), 0.0, 1.0
            )
            val animation = DoubleAnimation(
                animationClock, easingFunction
            )
            magicExtraWidthAnimations.add(
                PackedAnimation(
                    animationClock, animation
                )
            )
        }
    }

    private fun getMagicAvailableStatus(magic: Magic): LMagicAvailableStatus {
        val calculationContext = MagicCalculationContext.fromEntity(player, targetedEntity)
        return magic.availableStatus(calculationContext).toLegacyStatus()
    }

    /**
     * 26.2 port: [Magic.availableStatus] now returns a [MagicAvailability] (set of
     * [MagicAvailableStatus] entries) instead of the legacy [LMagicAvailableStatus] enum this
     * HUD renders with. Maps the first reported status onto the legacy enum, preserving the old
     * per-status color/animation behavior.
     */
    private fun MagicAvailability.toLegacyStatus(): LMagicAvailableStatus {
        val status = firstOrNull() ?: return LMagicAvailableStatus.AVAILABLE
        return when (status.identifier.path) {
            MagicAvailableStatus.InsufficientMana.identifier.path -> LMagicAvailableStatus.AVAILABLE_MANA_NOT_ENOUGH
            MagicAvailableStatus.TargetImmune.identifier.path -> LMagicAvailableStatus.TARGET_IMMUNE
            MagicAvailableStatus.Unavailable.identifier.path -> LMagicAvailableStatus.UNAVAILABLE
            MagicAvailableStatus.ChannelQueueFull.identifier.path -> LMagicAvailableStatus.CHANNEL_QUEUE_FULL
            MagicAvailableStatus.ChannelQueueLocked.identifier.path -> LMagicAvailableStatus.CHANNEL_QUEUE_LOCKED
            MagicAvailableStatus.TargetMissing.identifier.path -> LMagicAvailableStatus.TARGET_MISSING
            "sculk_catalyst_is_already_active" -> LMagicAvailableStatus.SCULK_CATALYST_IS_ALREADY_ACTIVE
            else -> LMagicAvailableStatus.UNAVAILABLE
        }
    }

    private fun renderMagicAvailableStatus(
        drawContext: GuiGraphicsExtractor,
        renderer: LegacyMatrixUIRenderer,
    ) {
        val status = getMagicAvailableStatus(selectedMagic)
        if (status == LMagicAvailableStatus.AVAILABLE || status == LMagicAvailableStatus.TARGET_MISSING || !shouldRenderHud()) {
            AvailableStatusTooltip.hide()
        } else {
            AvailableStatusTooltip.show()
        }

        AvailableStatusTooltip.render(drawContext, renderer, status)
    }

    private fun updateAimAssist(
        updateRotation: Boolean,
        tickCounter: DeltaTracker,
    ) {
        targetedEntity?.apply {
            if (aimEntity == null) {
                AimAssist.resetAnimation()
                aimEntity = targetedEntity
            }
        }

        if (!updateRotation) {
            return
        }

        aimEntity?.apply {
            val tickDelta = tickCounter.getGameTimeDeltaPartialTick(
                true
            )
            val x = lerp(
                tickDelta.toDouble(), xo, x
            )
            val y = lerp(
                tickDelta.toDouble(), yo, y
            )
            val z = lerp(
                tickDelta.toDouble(), zo, z
            )
            AimAssist.lookAt(
                Vec3(
                    x, y - 0.5, z
                ), tickDelta.toDouble()
            )
            AimAssist.applyRotation()
        }
    }

    private val isHudVisible
        get() = magicShownOpacityAnimation.animatedValue != .0

    private val isHudInvisible
        get() = magicShownOpacityAnimation.animatedValue == .0

    var useBloom = false
    var renderHud = false
    var useBlur = false

    private fun renderOtherHuds(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        renderWitherArmorHud(drawContext, tickCounter)
    }

    private fun onBeginHudRender(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        renderOtherHuds(drawContext, tickCounter)

        if (!isHudInvisible) {
            useBlur = true
            renderHud = true

            useBloom = !hudBloomThreshold.animatedValue.approximatelyEqual(1.0) &&
                    !magicShownOpacityAnimation.animatedValue.approximatelyEqual(.0)
        }

        val bloodPact = player.isBloodPactActive
        hudBloomThreshold.value = if (bloodPact) {
            .0
        } else {
            1.0
        }
    }

    private fun onHudRender(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        // 26.2 capture semantics: everything this callback extracts goes into a dedicated GUI
        // stratum; GuiRendererMixin renders that stratum into hudFramebuffer at draw time
        // (the 1.21 "bind hudFramebuffer while the HUD draws" equivalent) and then invokes
        // onHudCaptured() for the blur/shadow/bloom composite back onto the main target.
        // Use the extractor's own state instance — the one GuiRenderer will actually render.
        val matrixRenderState = drawContext.guiRenderState as MatrixGuiRenderState
        matrixRenderState.beginMatrixHudStratum()
        onBeginHudRender(drawContext, tickCounter)
        renderHud(drawContext, tickCounter)
        // Everything extracted past this marker (screens, tooltips, toasts) stays OUT of the
        // capture — without it the segment would run to the blur fence and swallow them.
        matrixRenderState.endMatrixHudStratum()
    }

    /**
     * Runs right after GuiRendererMixin clears [hudFramebuffer] and BEFORE the HUD stratum
     * renders into it: extraction-time framebuffer passes that must sit under the stratum's
     * own draws (the 1.21 in-place ordering) are flushed here.
     */
    @JvmStatic
    fun onHudCaptureBegin() {
        StatusHud.flushPendingRings()
        // 1.21 order within the HUD layer: progress rings, then the aim guide lines, then the
        // stratum's own panels/text draw over both.
        GuideLineRenderer.renderToCapturedHud(hudFramebuffer)
    }

    /**
     * Runs the 1.21 end-of-HUD composite (backdrop blur, drop shadow, HUD copy, bloom) onto
     * the main target. Called by GuiRendererMixin right after the HUD stratum has been
     * rendered into [hudFramebuffer] — during the actual GUI render pass, not extraction.
     */
    @JvmStatic
    fun onHudCaptured() {
        onEndHudRender()
    }

    private fun renderCandidateEntities(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        // 1.21 drew world-space DEBUG_LINES immediately here; the 26.2 deferred GUI pipeline
        // only extracts state, so this now records the line list (endpoints computed with the
        // extraction-time partial tick) and GuideLineRenderer draws it at render time from the
        // GameRendererMixin.beginRender hook, before the post chain runs over the main target.
        GuideLineRenderer.clear()
        if (!shouldRenderHud()) {
            return
        }
        val tickDelta = tickCounter.getGameTimeDeltaPartialTick(true)

        var from = player.getPosition(tickDelta).add(.0, player.boundingBox.ysize / 2, .0)// Vector2d(crosshairX.animatedValue + 7.5, crosshairY.animatedValue + 7.5)
        val targetedEntity = this.targetedEntity
        if (targetedEntity != null) {
            val to = targetedEntity.getPosition(tickDelta).add(.0, targetedEntity.boundingBox.ysize / 2, .0)
            GuideLineRenderer.addLine(from, to, ARGB.color(255, 25, 255, 25))
            from = to
        }
        for (candidateEntity in candidateAimAssistEntities) {
            val targetPosition = candidateEntity.getPosition(tickDelta).add(.0, candidateEntity.boundingBox.ysize / 2, .0)

            GuideLineRenderer.addLine(from, targetPosition, ARGB.color(255, 255, 25, 25))
            from = targetPosition
        }
    }

    private fun renderWitherArmorHud(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        StatusHud.onHudRender(drawContext, tickCounter)
    }

    private fun renderHud(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        if (selectedIndex - 1 !in MatrixClient.getPlayerMagics().indices ||
            previousIndex - 1 !in MatrixClient.getPlayerMagics().indices
        ) {
            selectedIndex = 1
            previousIndex = 1
        }

        val renderer = LegacyMatrixUIRenderer(drawContext)
        renderCandidateEntities(drawContext, tickCounter)
        checkVisibilityChanges()
        warp()
        processKeyInput()
        checkMagicAvailableStatus()

        performChannelMagicAnimation()
        if (!shouldRenderHud()) {
            fovZoomRatio = .0
            targetedEntity = null
        }
        fovAnimation.value = 1.0 - fovZoomRatio
        lastNanos = System.nanoTime()

        // magicShownAnimationClock.let {
        //     it.from = magicShownAnimation.animatedValue
        //     it.to = .0
        // }

        // magicShownOpacityAnimationClock.let {
        //     it.from = magicShownOpacityAnimation.animatedValue
        //     it.to = 1.0
        // }
        if (magicShownOpacityAnimation.animatedValue == .0) {
            for (animation in magicExtraWidthAnimations) {
                animation.animation.currentValue = .0
            }

            return
        }

        ManaCostTooltip.render(drawContext, tickCounter)
        renderLeftPart(drawContext, tickCounter)
        renderRightPart(drawContext, tickCounter)
        renderManaBar(drawContext, tickCounter)
        renderMagicAvailableStatus(drawContext, renderer)

        if (!shouldRenderHud()) {
            return
        }
        selectTargetEntity(tickCounter)
        renderOverclock(drawContext, tickCounter)

        updateAimAssist(false, tickCounter)
    }

    val points = FloatArray(1000 * 2) {
        if (it % 2 == 0) {
            Random.nextFloat() * 3f - 1f - 3F
        } else {
            1f - Random.nextFloat() * 0.2f
        }
    }

    private fun onEndHudRender() {
        if (!renderHud) {
            return
        }

        if (useBlur) {
            val strength = magicShownOpacityAnimation.animatedValue.toFloat()
            GaussianBlurRenderer.gaussianKernel = GaussianBlurRenderer.generateSymmetricGaussianKernel(strength, maxSigma = 8F)
            BlurRenderer.renderGaussianBlur(minecraft.mainRenderTarget)

            dropHudShadow()

            // Previously drawn while minecraft's framebuffer was bound with
            // (GL_ONE, GL_ONE_MINUS_SRC_ALPHA) blending; the target/blend now travel with the
            // copy calls inside renderHudBlur.
            renderHudBlur()
        } else {
            // Previously bound blurFramebuffer with (GL_ONE, GL_ONE_MINUS_SRC_ALPHA) blending;
            // copyFramebuffer(from, to, disableBlend = false) used the wrapper-set blend func.
            PostProcessRenderer.copyFramebuffer(hudFramebuffer, blurFramebuffer, BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA)
            PostProcessRenderer.copyFramebuffer(hudFramebuffer, minecraft.mainRenderTarget, BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA)
        }

        BloomEffect.brightnessPassFramebuffer = blurFramebuffer
        BloomEffect.brightnessThreshold = 1F

        // Old wrapper: BlendState(true) + BlendFuncState(GL_ONE, GL_ONE) -> additive composite.
        // The guide-line 8x energy joins exactly this pass (its 1.21 home was the HDR
        // hudFramebuffer content this brightness pass read); see BloomEffect doc.
        BloomEffect.includeGuideLineOverlay = true
        BloomEffect.renderBloom()
        BloomEffect.includeGuideLineOverlay = false
        PostProcessRenderer.copyFramebuffer(BloomEffect.bloomUpFramebuffer, minecraft.mainRenderTarget, BlendFunction.ADDITIVE)

        PostProcessRenderer.clear(hudFramebuffer)
        PostProcessRenderer.clear(blurFramebuffer)

        renderHud = false
        useBlur = false
        useBloom = false
    }

    private fun dropHudShadow() {
        GaussianBlurRenderer.gaussianKernel = GaussianBlurRenderer.generateSymmetricGaussianKernel(1.0F, maxSigma = 20F)
        BlurRenderer.renderGaussianBlur(hudFramebuffer, blurFramebuffer)

        // Render the blurred hud background to blurFramebuffer. The old code bound
        // blurFramebuffer via FramebufferState with (GL_ONE, GL_ONE_MINUS_SRC_ALPHA) blending,
        // which maps to TRANSLUCENT_PREMULTIPLIED_ALPHA on the wrapper API.
        OpacityMaskRenderer.render(
            hudFramebuffer, BlurRenderer.blurFramebuffer, blurFramebuffer,
            BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA
        )

        // Render half-transparent hud on the blurred hud background. 1.21 used vanilla
        // Framebuffer.draw here — NO discard — or the pure-black panel fills drop out and
        // the boxes lose their dark tint (the discarding copyFramebuffer path ate them).
        PostProcessRenderer.drawFramebuffer(hudFramebuffer, blurFramebuffer, BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA)
    }

    private fun renderHudBlur() {
        // Both branches ended in vanilla Framebuffer.draw pre-migration (no discard) — the
        // shadow's pure-black rgb must survive the composite onto the main target.
        val strength = 1.0F - magicShownOpacityAnimation.animatedValue.toFloat()
        if (strength == .0F) {
            PostProcessRenderer.drawFramebuffer(blurFramebuffer, minecraft.mainRenderTarget, BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA)
            return
        }

        GaussianBlurRenderer.gaussianKernel = GaussianBlurRenderer.generateSymmetricGaussianKernel(strength, maxSigma = 20F)
        BlurRenderer.renderGaussianBlurFullResolution(blurFramebuffer)
        PostProcessRenderer.drawFramebuffer(BlurRenderer.blurFramebuffer, minecraft.mainRenderTarget, BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA)
    }

    // TODO(26.2): renderPoints drew `points` as GL_POINTS with GL_PROGRAM_POINT_SIZE through raw
    // client vertex arrays and the transform-feedback pointSpriteProgram above. None of that has
    // a dual-backend wrapper equivalent (no point primitives, no transform feedback, no raw VAO
    // path), and the function was never called from any live code path; kept as a no-op stub so
    // the debug entry point (and the `points` data set) survives for a future reimplementation.
    @Suppress("unused", "UNUSED_PARAMETER")
    private fun renderPoints() {
    }

    private fun performChannelMagicAnimation() {
        val delta = System.nanoTime() - lastNanos
        for (magic in usingMagicList) {
            magic.setValue(
                magic.value + delta / 2000000
            ) // 2000000
        }
    }

    @JvmStatic
    fun shouldRenderHud(): Boolean {
        if (minecraft.player == null) {
            return false
        }
        if (MatrixClient.getPlayerMagics().isEmpty() || !player.isWizard) {
            return false
        }
        return Minecraft.getInstance().options.keyPlayerList.isDown
    }

    private val isPressingTab
        get() = Minecraft.getInstance().options.keyPlayerList.isDown

    @JvmField
    var isPressingRightMouseButton = false

    @JvmField
    var isPressingLeftMouseButton = false

    fun shouldSlowTime(): Boolean {
        val minecraft = Minecraft.getInstance()
        val server = minecraft.singleplayerServer
        return minecraft.isLocalServer && (server != null && !server.isPublished)
    }

    private fun onHudVisibilityChanged(visibility: Boolean) {
        manaBar.onHudVisibilityChanged(visibility)
        ManaCostTooltip.onHudVisibilityChanged(visibility)
        if (visibility) {
            onHudShown()
        } else {
            onHudHide()
        }
    }

    private fun onHudHide() {
        if (shouldSlowTime()) {
            magicTimeScale.value = 1.0
        }

        magicShownAnimationClock.let {
            it.from = magicShownAnimation.animatedValue
            it.to = -50.0
        }

        magicShownOpacityAnimationClock.let {
            it.from = magicShownOpacityAnimation.animatedValue
            it.to = .0
        }

        dissolveAnimation.value = 1.0
        dissolveAnimation.duration = Duration.ofMillis(300)
        dissolveAnimation.start()

        magicShownAnimationClock.start()
        magicShownOpacityAnimationClock.start()
        ManaCostTooltip.hide()

        fovAnimation.value = 1.0
    }

    private fun onHudShown() {
        // println(glGenProgramPipelines())
        if (shouldSlowTime()) {
            magicTimeScale.value = 0.01
        }

        magicShownAnimationClock.let {
            it.from = magicShownAnimation.animatedValue
            it.to = .0
        }

        magicShownOpacityAnimationClock.let {
            it.from = magicShownOpacityAnimation.animatedValue
            it.to = 1.0
        }

        dissolveAnimation.value = .0
        dissolveAnimation.duration = Duration.ofMillis(1000)
        dissolveAnimation.startTime = Duration.ofMillis(0)
        dissolveAnimation.start()

        magicShownAnimationClock.start()
        magicShownOpacityAnimationClock.start()

        ManaCostTooltip.show()
        AimAssist.resetAnimation()
        if (firstShow) {
            firstShow = false
            selectTargetEntity(DeltaTracker.ZERO)
            if (targetedEntity == null) {
                descriptionYOffsetAnimation.animatedValue = -35.0
            }
        }
    }

    private fun renderManaBar(
        drawContext: GuiGraphicsExtractor,
        tickCounter: DeltaTracker,
    ) {
        val calculationContext = MagicCalculationContext.fromEntity(player, targetedEntity)
        val magicCost = selectedMagic.getCost(calculationContext).toDouble()
        manaBar.manaCost.value = magicCost

        val renderer = LegacyMatrixUIRenderer(drawContext)
        manaBar.render(drawContext, renderer)
    }

    private fun renderLeftPart(
        drawContext: GuiGraphicsExtractor,
        tickCounter: DeltaTracker,
    ) {
        val magics = MatrixClient.getPlayerMagics()
        val indentList = generateIndentList(magics.size)
        fillExtraWidthAnimationList(
            magics
        )
        repeat(
            magics.size
        ) {
            renderMagic(drawContext, tickCounter, it + 1, magics[it], indentList)
        }
    }

    private fun restrictedSizedString(
        string: String,
        width: Double,
    ): String {
        val textRenderer = Minecraft.getInstance().font
        var length = 0
        var index = 0
        for (character in string) {
            length += textRenderer.width(
                character.toString()
            )
            if (length > width) {
                return string.take(index)
            }
            ++index
        }

        return string
    }

    private fun renderMagic(
        drawContext: GuiGraphicsExtractor,
        tickCounter: DeltaTracker,
        index: Int,
        magic: Magic,
        indentList: List<Double>,
    ) {
        val displayData = magicDisplayData[index - 1]
        val xIndent = indentList[index - 1].toInt()

        val animatedColor = magicColorAnimations.getOrNull(
            index - 1
        )
        val color = ARGB.color(
            (magicShownOpacityAnimation.animatedValue * 127.5).toInt(), animatedColor?.red?.animatedValue?.toInt() ?: 0, animatedColor?.green?.animatedValue?.toInt() ?: 0, animatedColor?.blue?.animatedValue?.toInt() ?: 0
        )

        val height = 20.0
        val margin = 5.0

        val startY = (index * (height + margin) + drawContext.guiHeight() / 2 - (indentList.size + 1) * (height + margin) / 2).toInt()
        val endY = (startY + height).toInt()

        val status = getMagicAvailableStatus(
            magic
        ).let {
            if (it == LMagicAvailableStatus.TARGET_MISSING) {
                LMagicAvailableStatus.AVAILABLE
            } else {
                it
            }
        }

        val calculationContext = MagicCalculationContext.fromEntity(player, targetedEntity)
        val normalCost = magic.getNormalCost()
        val cost = magic.getCost(calculationContext)
        if (displayData.previousCost != cost) {
            displayData.costChangedAnimation.value = .0
            displayData.previousCost = cost
        }
        if (displayData.costChangedAnimation.animatedValue == .0) {
            displayData.displayCost = cost
            displayData.costChangedAnimation.value = 1.0
        }
        if (displayData.previousStatus != status) {
            displayData.statusChangedAnimation.value = .0
            displayData.previousStatus = status
        }
        if (displayData.statusChangedAnimation.animatedValue == .0) {
            displayData.displayStatus = status
            displayData.statusChangedAnimation.value = 1.0
        }
        val costString =
            if (displayData.displayCost > normalCost) {
                "§c↑§r" + displayData.displayCost.toString()
            } else if (displayData.displayCost < normalCost) {
                "§a↓§r" + displayData.displayCost.toString()
            } else {
                displayData.displayCost.toString()
            }
        val statusString = displayData.displayStatus.description

        val textRenderer = Minecraft.getInstance().font
        displayData.costWidthAnimation.value = textRenderer.width(costString).toDouble()

        val magicNameWidth = textRenderer.width(magic.definition.name)
        val costStringWidth = displayData.costWidthAnimation.animatedValue
        val statusStringWidth = textRenderer.width(status.description)
        val extraWidth = magicNameWidth + costStringWidth + statusStringWidth + 30 /* padding */
        val extraWidthAnimation = magicExtraWidthAnimations[index - 1].animation
        extraWidthAnimation.currentValue = extraWidth

        drawContext.enableScissor(
            xIndent + 50 + magicShownAnimation.animatedValue.toInt(), startY, xIndent + 50 + extraWidthAnimation.animatedValue.toInt() + magicShownAnimation.animatedValue.toInt(), endY
        )

        val blurBackgroundStartX = xIndent + 50 + magicShownAnimation.animatedValue.toInt()
        val blurBackgroundEndX = xIndent + 50 + extraWidthAnimation.animatedValue.toInt() + magicShownAnimation.animatedValue.toInt()

        // 26.2: the POSITION_COLOR triangle-fan quad (drawn through the global position-color
        // program with blending) is an axis-aligned rectangle -- drawContext.fill is equivalent.
        drawContext.fill(blurBackgroundStartX, startY, blurBackgroundEndX, endY, color)
        // dissolveShader.disableShader()

        val alpha = magicShownOpacityAnimation.animatedValue * 255
        val foregroundColor = ARGB.color(alpha.toInt(), 255, 255, 255)
        if (alpha > 4) {
            drawContext.text(textRenderer, magic.definition.name, xIndent + 55 + magicShownAnimation.animatedValue.toInt(), startY + 5, foregroundColor, false)
        }

        val costAlpha = min(alpha, displayData.costChangedAnimation.animatedValue * 255)
        // println((displayData.costChangedAnimation.animatedValue * 255).toInt())
        if (costAlpha > 4) {
            val costForegroundColor = ARGB.color(costAlpha.toInt(), 255, 255, 255)
            drawContext.text(textRenderer, Component.literal(costString), xIndent + 65 + magicNameWidth + magicShownAnimation.animatedValue.toInt(), startY + 5, costForegroundColor, false)
        }

        val statusAlpha = min(alpha, displayData.statusChangedAnimation.animatedValue * 255)
        if (statusAlpha > 4) {
            val statusForegroundColor = ARGB.color(statusAlpha.toInt(), 255, 255, 255)
            drawContext.text(textRenderer, statusString, xIndent + 75 + magicNameWidth + costStringWidth.toInt() + magicShownAnimation.animatedValue.toInt(), startY + 5, statusForegroundColor, false)
        }
        drawContext.disableScissor()

        if (usingMagicList.containsKey(
                index
            )
        ) {
            val deltaTime = usingMagicList[index]!!

            val maxX = xIndent + 55 + extraWidthAnimation.animatedValue.toInt() + magicShownAnimation.animatedValue.toInt()

            val startX = (xIndent + deltaTime) + magicShownAnimation.animatedValue.toInt()
            val endX = startX + height

            if (startX > maxX) {
                usingMagicList.remove(
                    index
                )
                return
            }

            val animationProgress = (startX - xIndent - 50) / extraWidth

            // 26.2: the four POSITION_COLOR triangle-strip parallelograms are now drawn as
            // per-row fills through drawSlantedBand. The old RenderSystem.setShaderColor
            // (4x, 4x, 4x, 0.7 - progress) HDR multiplier is folded directly into the vertex
            // colors (rgb clamped to 255, alpha multiplied); the bloom-oriented >1.0 brightness
            // cannot be expressed in the LDR GUI color path.
            val shaderAlpha = (0.7 - animationProgress).coerceIn(.0, 1.0)
            val progressColor = ARGB.color(
                (magicShownOpacityAnimation.animatedValue * 128 * shaderAlpha).toInt().coerceIn(0, 255),
                100, 255, 100
            )
            val brightProgressColor = ARGB.color(
                ((magicShownOpacityAnimation.animatedValue * 255).coerceAtMost(255.0) * shaderAlpha).toInt().coerceIn(0, 255),
                100, 255, 100
            )

            drawContext.enableScissor(
                xIndent + 50 + magicShownAnimation.animatedValue.toInt(), startY, xIndent + 50 + extraWidthAnimation.animatedValue.toInt() + magicShownAnimation.animatedValue.toInt(), endY
            )

            // TODO(26.2): the first band originally faded horizontally (transparent -> green)
            // across the parallelogram via per-vertex colors; drawn solid at the brighter edge
            // color here (per-row fills cannot express a horizontal gradient).
            val halfTransparentColor = ARGB.color(
                (magicShownOpacityAnimation.animatedValue * 255 * (1.0 - animationProgress)).toInt().coerceIn(0, 255),
                0, 255, 0
            )
            drawSlantedBand(drawContext, startX, startY, endY, height, halfTransparentColor)
            drawSlantedBand(drawContext, startX, startY, endY, height, progressColor)
            drawSlantedBand(drawContext, startX + 20, startY, endY, height, progressColor)
            drawSlantedBand(drawContext, startX + 10, startY, endY, height, brightProgressColor)

            // val renderer = MatrixUIRenderer(drawContext.vertexConsumers)
            // renderer.renderRectangle(
            //     Rectangle(
            //         Point(startX, startY.toDouble()),
            //         Point(endX, endY.toDouble())
            //     ),
            //     Color(0, 0, 128, magicShownOpacityAnimation.animatedValue.toInt())
            // )
            drawContext.disableScissor()
        }
    }

    /**
     * Draws the slanted progress parallelogram (bottom edge at [startX], top edge shifted right
     * by [bandWidth]) as per-row 1px fills -- the 26.2 replacement for the old POSITION_COLOR
     * triangle strips.
     */
    private fun drawSlantedBand(
        drawContext: GuiGraphicsExtractor,
        startX: Double,
        top: Int,
        bottom: Int,
        bandWidth: Double,
        color: Int,
    ) {
        val rows = (bottom - top).coerceAtLeast(1)
        for (row in 0 until rows) {
            val rowFraction = row / rows.toDouble()
            val left = startX + bandWidth * (1.0 - rowFraction)
            val right = left + bandWidth
            drawContext.fill(
                left.roundToInt(),
                top + row,
                right.roundToInt().coerceAtLeast(left.roundToInt() + 1),
                top + row + 1,
                color
            )
        }
    }

    private fun renderOverclock(
        drawContext: GuiGraphicsExtractor,
        tickCounter: DeltaTracker,
    ) {
        val renderer = LegacyMatrixUIRenderer(
            drawContext
        )
        val manaOverclockMinPoint = Point(
            renderer.scaledWindowWidth - 5.0, renderer.scaledWindowHeight.toDouble() - 25.0
        )
        val manaOverclockMaxPoint = Point(
            renderer.scaledWindowWidth - 10.0, Mth.lerp(
                manaOverclock.animatedValue / 10, renderer.scaledWindowHeight.toDouble() - 25.0, 25.0
            )
        )

        val magicOverclockMinPoint = Point(
            renderer.scaledWindowWidth - 15.0, renderer.scaledWindowHeight.toDouble() - 25.0
        )

        val magicOverclockMaxPoint = Point(
            renderer.scaledWindowWidth - 20.0, Mth.lerp(
                magicOverclock.animatedValue / 10, renderer.scaledWindowHeight.toDouble() - 25.0, 25.0
            )
        )

        val magicOverclockColor = Color(
            Mth.lerp(
                magicOverclock.animatedValue / 10.0, 0.0, 255.0
            ).toInt(), Mth.lerp(
                magicOverclock.animatedValue / 10.0, 255.0, 0.0
            ).toInt(), 0, 128
        )

        val manaOverclockColor = Color(
            Mth.lerp(
                manaOverclock.animatedValue / 10.0, 0.0, 255.0
            ).toInt(), Mth.lerp(
                manaOverclock.animatedValue / 10.0, 255.0, 0.0
            ).toInt(), 0, 128
        )

        renderer.renderRectangle(
            Rectangle(
                manaOverclockMinPoint, manaOverclockMaxPoint
            ), manaOverclockColor
        )
        renderer.renderRectangle(
            Rectangle(
                magicOverclockMinPoint, magicOverclockMaxPoint
            ), magicOverclockColor
        )
    }

    private fun selectTargetEntity(tickCounter: DeltaTracker) {
        var targetedEntity = getTargetedEntity(tickCounter.getGameTimeDeltaPartialTick(true))
        if (targetedEntity is EnderDragonPart) {
            targetedEntity = targetedEntity.parentMob
        }
        if (targetedEntity != this.targetedEntity) {
            if (targetedEntity == null) {
                entityDescriptionOpacityAnimation.value = .0
                descriptionYOffsetAnimation.value = -35.0
            } else {
                entityDescriptionOpacityAnimation.value = 1.0
                descriptionYOffsetAnimation.value = .0
            }
        }
        if (targetedEntity is LivingEntity) {
            cachedTargetedEntity = targetedEntity
        }
        this.targetedEntity = targetedEntity as? LivingEntity?
    }

    private fun renderRightPart(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        val backgroundColor = ARGB.color((magicShownOpacityAnimation.animatedValue * 127.5).toInt(), 255, 255, 255)
        val alpha = (magicShownOpacityAnimation.animatedValue * 255).coerceIn(.0..255.0).toInt()

        val offsetX = magicShownAnimation.animatedValue.toFloat()
        val offsetY = descriptionExtraHeightAnimation.animatedValue.toFloat()

        val windowWidth = drawContext.guiWidth()
        val windowHeight = drawContext.guiHeight()

        val leftX = windowWidth - 200 - offsetX
        val rightX = windowWidth - 25 - offsetX
        val topY = windowHeight / 2 - 100F - offsetY / 2F
        val bottomY = windowHeight / 2 + 100F + offsetY / 2F

        val width = rightX - leftX
        val height = bottomY - topY

        // 26.2: the 1.21 mesh-attached dissolve program (DissolveShader around a
        // POSITION_TEXTURE_COLOR quad) is a GUI-pipeline element now; the per-draw uniforms
        // (factor, height/width ratio, time) travel packed in the Normal attribute.
        DissolveRect.precompile()
        drawContext.guiRenderState.addGuiElement(
            DissolveRectRenderState(
                leftX, topY, rightX, bottomY,
                backgroundColor,
                dissolveAnimation.animatedValue.toFloat(),
                height / width,
                (System.nanoTime() % 10_000_000_000L) / 1_000_000_000F,
                scissor = null,
            )
        )

        val descriptionAlpha = min(magicShownOpacityAnimation.animatedValue * 255, entityDescriptionOpacityAnimation.animatedValue * 255).toInt()
        val cachedTargetedEntity = this.cachedTargetedEntity

        val textRenderer = Minecraft.getInstance().font
        if (cachedTargetedEntity != null) {
            val red = ARGB.color(descriptionAlpha, 255, 25, 25)
            val green = ARGB.color(descriptionAlpha, 25, 255, 25)

            val health = cachedTargetedEntity.health.toDouble()
            val maxHealth = cachedTargetedEntity.maxHealth.toDouble()

            if (health.isFinite()) {
                healthAnimation.value = health
            }
            if (maxHealth.isFinite()) {
                maxHealthAnimation.value = maxHealth
            }
            healthPercentageAnimation.value = (health / maxHealth).coerceIn(.0..1.0)

            val percentage = healthPercentageAnimation.animatedValue
            val lerpedColor = lerpColor(percentage.toFloat(), red, green)

            val sizeReduced = if (sizeScalingAnimation) {
                10 - (entityDescriptionOpacityAnimation.animatedValue * 10).toInt()
            } else {
                0
            }

            val progressBarX = Mth.lerpInt(percentage.toFloat(), 190, 35)
            val x1 = drawContext.guiWidth() - 190 - magicShownAnimation.animatedValue.toInt() + sizeReduced
            val x2 = drawContext.guiWidth() - progressBarX - magicShownAnimation.animatedValue.toInt() - sizeReduced

            // TODO(26.2): the health bar fill was brightened up to 5x via
            // RenderSystem.setShaderColor (HDR input for the bloom pass); the LDR GUI color path
            // cannot express >1.0 channel multipliers, so the boost is dropped.
            drawContext.fill(
                x1.coerceAtMost(x2),
                drawContext.guiHeight() / 2 - 80 - (descriptionExtraHeightAnimation.animatedValue / 2).toInt() - sizeReduced,
                x2,
                drawContext.guiHeight() / 2 - 75 - (descriptionExtraHeightAnimation.animatedValue / 2).toInt() - sizeReduced,
                lerpedColor
            )

            val fontSizeReduced = if (sizeScalingAnimation) {
                entityDescriptionOpacityAnimation.animatedValue.toFloat()
            } else {
                1F
            }
            val descriptionForegroundColor = ARGB.color(descriptionAlpha, 255, 255, 255)
            if (descriptionAlpha > 3) {
                val currentHealth = BigDecimal.valueOf(healthAnimation.animatedValue)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString()
                val maxHealth = BigDecimal.valueOf(maxHealthAnimation.animatedValue)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString()
                val healthText = StringBuilder()
                    .append(currentHealth)
                    .append("/")
                    .append(maxHealth)
                    .toString()

                val textX = drawContext.guiWidth() - 190 - magicShownAnimation.animatedValue.toInt()
                val textY = drawContext.guiHeight() / 2 - 70 - (descriptionExtraHeightAnimation.animatedValue / 2).toInt()

                val textWidth = textRenderer.width(healthText)
                val textCenterX = textX + textWidth / 2.0
                val textCenterY = textY.toDouble() - 15

                drawContext.pose().pushMatrix()

                drawContext.pose().translate(textCenterX.toFloat(), textCenterY.toFloat())
                drawContext.pose().scale(fontSizeReduced, fontSizeReduced)
                drawContext.pose().translate(-textCenterX.toFloat(), -textCenterY.toFloat())

                drawContext.text(
                    textRenderer,
                    healthText,
                    textX,
                    textY,
                    descriptionForegroundColor,
                    false
                )
                drawContext.pose().popMatrix()
            }

            val hashCode = cachedTargetedEntity.name.hashCode()
            if (hashCode != previousDisplayEntityNameHashCode) {
                displayEntityNameOpacityAnimation.value = .0
                previousDisplayEntityNameHashCode = hashCode
            }
            if (displayEntityNameOpacityAnimation.animatedValue == .0) {
                displayEntityNameOpacityAnimation.value = 1.0
                displayEntityName = cachedTargetedEntity.name
            }
            val entityNameAlpha = min(descriptionAlpha, (displayEntityNameOpacityAnimation.animatedValue * 255).toInt())
            val entityNameForegroundColor = ARGB.color(entityNameAlpha, 255, 255, 255)
            if (entityNameAlpha > 3) {
                val textX = drawContext.guiWidth() - 190 - magicShownAnimation.animatedValue.toInt()
                val textY = drawContext.guiHeight() / 2 - 90 - (descriptionExtraHeightAnimation.animatedValue / 2).toInt()

                val textWidth = textRenderer.width(displayEntityName)
                val textCenterX = textX + textWidth / 2.0
                val textCenterY = textY + textRenderer.lineHeight / 2.0

                drawContext.pose().pushMatrix()

                drawContext.pose().translate(textCenterX.toFloat(), textCenterY.toFloat())
                drawContext.pose().scale(fontSizeReduced, fontSizeReduced)
                drawContext.pose().translate(-textCenterX.toFloat(), -textCenterY.toFloat())

                drawContext.text(textRenderer, displayEntityName, textX, textY, entityNameForegroundColor, true)

                drawContext.pose().popMatrix()
            }

            if (candidateAimAssistEntities.size != previousCandidateEntities) {
                displayCandidateCountOpacityAnimation.value = .0
                previousCandidateEntities = candidateAimAssistEntities.size
            } else if (displayCandidateCountOpacityAnimation.animatedValue == .0) {
                displayCandidateCountOpacityAnimation.value = 1.0
                displayCandidateEntities = candidateAimAssistEntities.size
            }

            val candidateCountAlpha = min(descriptionAlpha, (displayCandidateCountOpacityAnimation.animatedValue * 255).toInt())
            val candidateCountColor = ARGB.color(candidateCountAlpha, 255, 255, 255)
            if (candidateCountAlpha > 3) {
                candidateCountPadding.value = (textRenderer.width(displayEntityName) + textRenderer.width(" ")).toDouble()
                val padding = candidateCountPadding.animatedValue
                val textX = drawContext.guiWidth() - 190 - magicShownAnimation.animatedValue.toInt() + padding
                val textY = drawContext.guiHeight() / 2 - 90 - (descriptionExtraHeightAnimation.animatedValue / 2).toInt()

                val text = "+$displayCandidateEntities"
                val textWidth = textRenderer.width(text)
                val textCenterX = textX + textWidth / 2.0
                val textCenterY = textY + textRenderer.lineHeight / 2.0

                drawContext.pose().pushMatrix()

                drawContext.pose().translate(textCenterX.toFloat(), textCenterY.toFloat())
                drawContext.pose().scale(fontSizeReduced, fontSizeReduced)
                drawContext.pose().translate(-textCenterX.toFloat(), -textCenterY.toFloat())

                drawContext.text(textRenderer, text, round(textX).toInt(), textY, candidateCountColor, true)

                drawContext.pose().popMatrix()
            }
        }

        if (alpha <= 3) {
            return
        }

        drawContext.enableScissor(
            drawContext.guiWidth() - 200 - magicShownAnimation.animatedValue.toInt(),
            drawContext.guiHeight() / 2 - 100 - (descriptionExtraHeightAnimation.animatedValue / 2).toInt(),
            drawContext.guiWidth() - 25 - magicShownAnimation.animatedValue.toInt(),
            drawContext.guiHeight() / 2 + 100 + (descriptionExtraHeightAnimation.animatedValue / 2).toInt()
        )
        val currentMagic = MatrixClient.getPlayerMagics()[selectedIndex - 1]

        if (currentMagic.definition.description != currentDescription) {
            currentDescription = currentMagic.definition.description
            magicDescriptionChangedAnimation.value = .0
        }
        if (magicDescriptionChangedAnimation.animatedValue == .0) {
            magicDescriptionChangedAnimation.value = 1.0
            displayDescription = currentDescription
        }

        val descriptionY = drawContext.guiHeight() / 2 - 55 + descriptionYOffsetAnimation.animatedValue.toInt() - (descriptionExtraHeightAnimation.animatedValue / 2).toInt() // + magicSwitchAnimation.animatedValue

        val lines = textRenderer.split(currentDescription, 150)
        val extraHeight = textRenderer.lineHeight * lines.size - 180.0
        descriptionExtraHeightAnimation.value = extraHeight.coerceAtLeast(.0)

        val magicDescriptionAlpha = (min(magicShownOpacityAnimation.animatedValue, magicDescriptionChangedAnimation.animatedValue) * 255).toInt()
        if (magicDescriptionAlpha > 3) {
            drawContext.textWithWordWrap(textRenderer, displayDescription, drawContext.guiWidth() - 190 - magicShownAnimation.animatedValue.toInt(), descriptionY, 150, ARGB.color(magicDescriptionAlpha, 255, 255, 255))
        }
        drawContext.disableScissor()
    }

    /**
     * 26.2 replacement for the removed ColorHelper.Argb.lerp: per-channel ARGB interpolation.
     */
    private fun lerpColor(delta: Float, from: Int, to: Int): Int {
        return ARGB.color(
            Mth.lerpInt(delta, ARGB.alpha(from), ARGB.alpha(to)),
            Mth.lerpInt(delta, ARGB.red(from), ARGB.red(to)),
            Mth.lerpInt(delta, ARGB.green(from), ARGB.green(to)),
            Mth.lerpInt(delta, ARGB.blue(from), ARGB.blue(to)),
        )
    }

    private fun getTargetedEntity(tickDelta: Float): Entity? {
        val candidateEntities = getAssistTargetEntity(tickDelta)//.filter { entity ->
        //    val rotationVector = player.getRotationVec(tickDelta)
        //    val from = player.getCameraPosVec(tickDelta)
        //    val to = player.getCameraPosVec(tickDelta).add(rotationVector.multiply(aimAssistMaxDistance))
        //    val blockHit = player.world.raycast(ClipContext(from, to, ClipContext.ShapeType.VISUAL, ClipContext.FluidHandling.ANY, player))
        //    val blockHitDistance = player.squaredDistanceTo(blockHit.pos)
        //    blockHitDistance >= entity.squaredDistanceTo(player)
        //}
        candidateAimAssistEntities = candidateEntities

        val rayCastTarget = getRayCastTargetEntity(tickDelta)
        if (rayCastTarget != null) {
            usingRayCast = true
            return rayCastTarget
        }
        usingRayCast = false
        return candidateEntities.firstOrNull()
    }

    private fun getAssistTargetEntity(tickDelta: Float): List<LivingEntity> {
        val entities = minecraft.cameraEntity?.getEntitiesNearSight(
            aimAssistMaxDistance,
            aimAssistFov,
            tickDelta
        ) // TODO: Sort by distance
        if (entities == null) {
            return emptyList()
        }

        return entities
            .filter { it is LivingEntity && !it.isSpectator && it.isAlive }
            .map { it as LivingEntity }
            .filter {
                val calculationContext = MagicCalculationContext.fromEntity(player, it)
                selectedMagic.availableStatus(calculationContext).isAvailable
            }
            .toList()
    }

    private fun getRayCastTargetEntity(tickDelta: Float): Entity? {
        val range = 10000.0

        val minecraftClient = Minecraft.getInstance()!!
        val camera = minecraftClient.cameraEntity!!

        val location = camera.getEyePosition(tickDelta)
        val rotation = camera.getViewVector(tickDelta)
        val min = location.add(rotation)
        val max = location.add(rotation.scale(range))
        val box = AABB(min, max)

        // Wrap Dancer: Select entities through walls
        if (player.wizardHelmetStack.item is WizardHelmet5) {
            val result = ProjectileUtil.getEntityHitResult(camera, min, max, box, { entity ->
                !entity.isSpectator
            }, range)
            return result?.entity
        }

        val blockHit = player.level().clip(ClipContext(min, max, ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, player))
        val blockHitDistance = player.distanceToSqr(blockHit.location)
        val result = ProjectileUtil.getEntityHitResult(camera, min, max, box, { entity ->
            !entity.isSpectator && blockHitDistance > entity.distanceToSqr(player)
        }, range)

        return result?.entity
    }

    private val crosshairX = SimpleDoubleAnimation()
    private val crosshairY = SimpleDoubleAnimation()

    @JvmStatic
    fun translateCrosshairPosition(x: Float, y: Float): Vector2f {
        if (!aimAssistEnabled) {
            return Vector2f(x, y)
        }

        if (crosshairX.from == .0) {
            crosshairX.from = x.toDouble()
        }
        if (crosshairY.from == .0) {
            crosshairY.from = y.toDouble()
        }
        val targetedEntity = targetedEntity
        if (targetedEntity == null) {
            crosshairX.value = x.toDouble()
            crosshairY.value = y.toDouble()
        } else {
            val tickDelta = minecraft.deltaTracker.getGameTimeDeltaPartialTick(true)
            val lerpedPosition = targetedEntity.getPosition(tickDelta).add(
                .0,
                targetedEntity.boundingBox.ysize / 2,
                .0
            )
            // The blitSprite coordinates this feeds are GUI-scaled, so project into the
            // gui-scaled viewport (the physical-pixel default lands scale-factor off-center).
            val screenPosition = worldToScreen(
                lerpedPosition,
                viewportWidth = minecraft.window.guiScaledWidth,
                viewportHeight = minecraft.window.guiScaledHeight,
            )
            if (screenPosition != null) {
                crosshairX.value = screenPosition.x
                crosshairY.value = screenPosition.y
            }
        }

        return Vector2f(crosshairX.animatedValue.toFloat(), crosshairY.animatedValue.toFloat())
    }

    private fun generateIndentList(size: Int): List<Double> {
        val result = mutableListOf<Double>()

        val ease = ElasticEase()
        ease.apply {
            oscillations = 0
            springiness = 1.0
            easingMode = EasingMode.OUT
        }

        // var current = 0
        val center = size / 2.0
        for (i in 0 until size) {
            val distance = abs(center - i)
            val current = 1 - ease.transform(1 - distance.inverseLerp(.00..center))

            result.add(current * 50 - 25)

            // if (size % 2 == 0 && i == size / 2 - 1) {
            //     continue
            // }
//
            // if (i >= size / 2) {
            //     current += 5
            // } else {
            //     current -= 5
            // }
        }

        return result
    }

    fun onRemoteManaUpdate() {
        manaBar.onRemoteManaUpdate()
    }

    @JvmStatic
    fun onKey(window: Long, key: Int, scancode: Int, action: Int, mods: Int): Boolean {
        if (window != minecraft.window.handle()) {
            return false
        }

        // Only the E key's own PRESS event may be consumed: these branches used to match any
        // key event while E was physically held (old-jar parity), which swallowed the TAB
        // RELEASE when E was still down — vanilla never saw the release, keyPlayerList.isDown
        // stayed true and the slow-time stuck on ("state not controlled" bug).
        val isEKeyPress = key == GLFW.GLFW_KEY_E && action == GLFW.GLFW_PRESS
        if (isEKeyPress && isPressingTab &&
            player.getItemBySlot(EquipmentSlot.CHEST).item is LightningChestplate1
        ) {
            // 26.2: `BorrowedTimePayload()` was an unresolved reference even in the 1.21 baseline
            // (70b7cff); the registered serverbound payload is the ServerboundBorrowedTimePayload
            // data object.
            ClientPlayNetworking.send(ServerboundBorrowedTimePayload)
            return true
        }

        if (isEKeyPress && shouldRenderHud()) {
            ClientPlayNetworking.send(ServerboundActivateBloodPactPayload)
            return true
        }

        if (!KeyEvent.EVENT.invoker().onKey(key, scancode, action, mods)) {
            return true
        }
        return false
    }

    private var debug = false

    @JvmStatic
    fun onMouseButton(window: Long, button: Int, action: Int, mods: Int): Boolean {
        if (window != minecraft.window.handle()) {
            return false
        }

        if (shouldRenderHud() && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (action == GLFW.GLFW_PRESS) {
                debug = true

                fovZoomRatio += 0.2
                isPressingRightMouseButton = true
                return true
            } else if (action == GLFW.GLFW_RELEASE) {
                fovZoomRatio = .0
                isPressingRightMouseButton = false
                return true
            }
        }

        if (!MouseButtonEvent.EVENT.invoker().onMouseButton(button, action, mods)) {
            return true
        }
        return false
    }

    @JvmStatic
    fun nextZoomLevel() {
        fovZoomRatio = (fovZoomRatio + 0.2).coerceAtMost(0.95)
    }

    @JvmStatic
    fun previousZoomLevel() {
        fovZoomRatio = (fovZoomRatio - 0.2).coerceAtLeast(.0)
    }
}