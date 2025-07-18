package heckerpowered.matrix.client

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.core.AimAssist
import heckerpowered.matrix.client.core.ClientOptions.aimAssistEnabled
import heckerpowered.matrix.client.core.ClientOptions.aimAssistFov
import heckerpowered.matrix.client.core.ClientOptions.aimAssistMaxDistance
import heckerpowered.matrix.client.event.KeyEvent
import heckerpowered.matrix.client.event.MouseButtonEvent
import heckerpowered.matrix.client.render.*
import heckerpowered.matrix.client.render.post.BloomEffect
import heckerpowered.matrix.client.render.shader.GaussianBlurRenderer
import heckerpowered.matrix.client.render.shader.opacityMask
import heckerpowered.matrix.client.render.state.*
import heckerpowered.matrix.client.render.state.capabilities.BlendState
import heckerpowered.matrix.client.render.state.capabilities.CullFaceState
import heckerpowered.matrix.client.render.state.capabilities.DepthTestState
import heckerpowered.matrix.client.shader.*
import heckerpowered.matrix.client.shader.component.TransformFeedback
import heckerpowered.matrix.client.ui.element.*
import heckerpowered.matrix.client.ui.foundation.animation.*
import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.effect.bloodPactActive
import heckerpowered.matrix.common.item.LightningChestplate1
import heckerpowered.matrix.common.item.LightningChestplate1.isBorrowedTime
import heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking
import heckerpowered.matrix.common.item.WizardHelmet5
import heckerpowered.matrix.common.magics.MagicAvailableStatus
import heckerpowered.matrix.common.magics.MagicAvailableStatus.AVAILABLE
import heckerpowered.matrix.common.magics.MagicAvailableStatus.TARGET_MISSING
import heckerpowered.matrix.common.magics.description
import heckerpowered.matrix.common.network.ActiveBloodPactPayload
import heckerpowered.matrix.common.network.BorrowedTimePayload
import heckerpowered.matrix.common.network.OverclockPayload
import heckerpowered.matrix.common.network.UseMagicPayload
import heckerpowered.matrix.common.persistent.getChannelSequence
import heckerpowered.matrix.common.persistent.isWizard
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.core.*
import heckerpowered.matrix.core.utility.EntitySearch.getEntitiesNearSight
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.dragon.EnderDragonPart
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Util
import net.minecraft.util.math.Box
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import org.joml.Vector2d
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL15.glDeleteBuffers
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL46.*
import org.lwjgl.system.MemoryUtil
import java.time.Duration
import kotlin.math.abs
import kotlin.math.min
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
    private var displayEntityName: Text = Text.empty()
    private var displayEntityNameOpacityAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(150), initValue = 1.0)

    private val manaOverclock = DoubleAnimation(
        manaOverclockAnimation, easingFunction
    )
    private val magicOverclock = DoubleAnimation(
        magicOverclockAnimation, easingFunction
    )

    private val dissolveShader by lazy {
        DissolveShader()
    }

    private val dissolveAnimation = SimpleDoubleAnimation(from = 1.0)

    private val magicDescriptionChangedAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(150))
    private var currentDescription: Text = Text.empty()
    private var displayDescription: Text = Text.empty()

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
    private val hudFramebuffer by lazy { PostProcessRenderer.createManagedFramebuffer() }
    private val blurFramebuffer by lazy { PostProcessRenderer.createManagedFramebuffer() }
    private val emissiveFramebuffer by lazy { PostProcessRenderer.createManagedFramebuffer() }

    private class MagicDisplayData {
        val statusChangedAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(150))
        val costChangedAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(150))
        val costWidthAnimation = SimpleDoubleAnimation()

        var displayCost = 0L
        var previousCost = 0L
        var displayStatus = AVAILABLE
        var previousStatus = AVAILABLE
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

    private val theWorldShader by lazy {
        BlitShader(
            resourceToString("/assets/matrix/shaders/sobel.vert"),
            resourceToString("/assets/matrix/shaders/post/the_world.fsh"),
            arrayOf(
                PostProcessRenderer.framebufferProvider,
                UniformProvider("grayscaleIntensity") { pointer ->
                    glUniform1f(pointer, grayscaleIntensityAnimation.animatedValue.toFloat())
                }
            )
        )
    }

    private val pointSpriteShader by lazy {
        Shader(
            resourceToString("/assets/matrix/shaders/point_sprite/point_sprite.vsh"),
            resourceToString("/assets/matrix/shaders/point_sprite/point_sprite.fsh"),
            uniforms = arrayOf(UniformProvider("time") { pointer ->
                val timeSeconds = System.nanoTime() / 1_000_000_000.0
                glUniform1f(pointer, timeSeconds.toFloat())
            }),
            components = arrayOf(
                TransformFeedback(
                    arrayOf("OutPosition"),
                    (points.size / 2),
                    bufferSize = (points.size / 2).toLong() * 4 * 2
                )
            )
        )
    }

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
        if (MatrixKeyBindings.useMagic.wasPressed()) {
            useCurrentMagic()
        }
        while (MatrixKeyBindings.useMagic.wasPressed()) {
            useCurrentMagic()
        }

        while (MatrixKeyBindings.nextMagic.wasPressed()) {
            nextMagic()
        }

        while (MatrixKeyBindings.previousMagic.wasPressed()) {
            previousMagic()
        }

        while (MatrixKeyBindings.overclockMagic.wasPressed()) {
            val difference = if (player.isSneaking) {
                -0.5
            } else {
                0.5
            }
            val newOverclock = (magicOverclock.currentValue + difference).coerceIn(
                1.0..10.0
            )
            magicOverclock.currentValue = newOverclock
            ClientPlayNetworking.send(OverclockPayload(manaOverclock.currentValue, magicOverclock.currentValue))
        }

        while (MatrixKeyBindings.overclockMana.wasPressed()) {
            val difference = if (player.isSneaking) {
                -0.5
            } else {
                0.5
            }
            val newOverclock = (manaOverclock.currentValue + difference).coerceIn(
                1.0..10.0
            )
            manaOverclock.currentValue = newOverclock
            ClientPlayNetworking.send(
                OverclockPayload(
                    manaOverclock.currentValue, magicOverclock.currentValue
                )
            )
        }
    }

    fun onInitialize() {
        HudRenderCallback.EVENT.register(this::onHudRender)
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
        ClientPlayNetworking.send(
            OverclockPayload(
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
        ClientPlayNetworking.send(
            OverclockPayload(
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
        ClientPlayNetworking.send(
            OverclockPayload(
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
        ClientPlayNetworking.send(
            OverclockPayload(
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
        val target = this.targetedEntity
        val channelSequence = player.getChannelSequence(target)
        manaBar.manaCost.value = magic.getCost(player, target, channelSequence).toDouble()
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
        if (magic.availableStatus(player, target, player.getChannelSequence(target)) != AVAILABLE) {
            return
        }

        magicColorAnimations[index].setColorWithoutAnimation(.0, 255.0, .0)

        // Reset use magic animation
        usingMagicList[index + 1] = 0.0

        val cost = magic.getCost(player, target, player.getChannelSequence(target))
        val mana = mana - manaUsage
        if (player.bloodPactActive && mana < cost) {
            minecraft.world!!.playSound(player, player.x, player.y, player.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1f)
            minecraft.world!!.playSound(player, player.x, player.y, player.z, SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.0f, 1f)
        } else {
            minecraft.world!!.playSound(player, player.x, player.y, player.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1f)
        }

        channelMagic(magic, target)
    }

    private fun channelMagic(magic: Magic, target: LivingEntity) {
        ClientPlayNetworking.send(UseMagicPayload(magic.id, target.id))
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
                TARGET_MISSING -> magicColorAnimations[index].setColor(
                    .0, .0, .0
                )

                AVAILABLE -> magicColorAnimations[index].setColor(
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

    private fun getMagicAvailableStatus(
        magic: Magic,
    ): MagicAvailableStatus {
        val channelSequence = player.getChannelSequence(
            targetedEntity
        )
        return magic.availableStatus(
            player, targetedEntity, channelSequence
        )
    }

    private fun renderMagicAvailableStatus(
        drawContext: DrawContext,
        renderer: LegacyMatrixUIRenderer,
    ) {
        val status = getMagicAvailableStatus(
            selectedMagic
        )
        if (status == AVAILABLE || status == TARGET_MISSING || !shouldRenderHud()) {
            AvailableStatusTooltip.hide()
        } else {
            AvailableStatusTooltip.show()
        }

        AvailableStatusTooltip.render(
            drawContext, renderer, status
        )
    }

    private fun updateAimAssist(
        updateRotation: Boolean,
        tickCounter: RenderTickCounter,
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
            val tickDelta = tickCounter.getTickDelta(
                true
            )
            val x = lerp(
                tickDelta.toDouble(), prevX, x
            )
            val y = lerp(
                tickDelta.toDouble(), prevY, y
            )
            val z = lerp(
                tickDelta.toDouble(), prevZ, z
            )
            AimAssist.lookAt(
                Vec3d(
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

    private fun renderOtherHuds(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        renderWitherArmorHud(drawContext, tickCounter)
    }

    private fun onBeginHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        renderOtherHuds(drawContext, tickCounter)

        if (!isHudInvisible) {
            useBlur = true
            renderHud = true

            useBloom = !hudBloomThreshold.animatedValue.approximatelyEqual(1.0) &&
                    !magicShownOpacityAnimation.animatedValue.approximatelyEqual(.0)
        }

        val bloodPact = player.bloodPactActive
        hudBloomThreshold.value = if (bloodPact) {
            .0
        } else {
            1.0
        }
    }

    private fun onHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        StateIsolation.isolate(FramebufferState(hudFramebuffer), ViewportState(hudFramebuffer)) {
            onBeginHudRender(drawContext, tickCounter)
            renderHud(drawContext, tickCounter)
            onEndHudRender(drawContext, tickCounter)
        }
    }

    private fun renderCandidateEntities(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        if (!shouldRenderHud()) {
            return
        }
        val tickDelta = tickCounter.getTickDelta(true)

        val transformationMatrix = drawContext.matrices.peek().positionMatrix
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR)

        val from = Vector2d(crosshairX.animatedValue + 7.5, crosshairY.animatedValue + 7.5)
        for (candidateEntity in candidateAimAssistEntities) {
            val targetPosition = candidateEntity.getLerpedPos(tickDelta).add(.0, candidateEntity.boundingBox.lengthY / 2, .0)
            val to = worldToScreen(targetPosition) ?: continue

            buffer.vertex(transformationMatrix, from.x.toFloat(), from.y.toFloat(), .0F)
                .color(255, 25, 25, 255)
            buffer.vertex(transformationMatrix, to.x.toFloat(), to.y.toFloat(), .0F)
                .color(255, 25, 25, 255)
        }

        StateIsolation.isolate(
            LineWidthState(3.0F),
            BlendState(true),
            BlendFuncSeparateState(),
            DepthTestState(false),
            CullFaceState(false),
            MinecraftShaderState(GameRenderer::getPositionColorProgram)
        ) {
            buffer.endNullable()?.let {
                BufferRenderer.drawWithGlobalProgram(it)
            }
        }
    }

    private fun renderWitherArmorHud(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        StatusHud.onHudRender(drawContext, tickCounter)
    }

    private fun renderHud(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        if (selectedIndex - 1 !in MatrixClient.getPlayerMagics().indices ||
            previousIndex - 1 !in MatrixClient.getPlayerMagics().indices
        ) {
            selectedIndex = 1
            previousIndex = 1
        }

        val renderer = LegacyMatrixUIRenderer(drawContext.vertexConsumers)
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
        lastNanos = Util.getMeasuringTimeNano()

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

    private fun onEndHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        if (!renderHud) {
            return
        }

        if (useBlur) {
            val strength = magicShownOpacityAnimation.animatedValue.toFloat()
            GaussianBlurRenderer.gaussianKernel = GaussianBlurRenderer.generateSymmetricGaussianKernel(strength, maxSigma = 8F)
            BlurRenderer.renderGaussianBlur(minecraft.framebuffer)

            dropHudShadow()

            StateIsolation.isolate(
                FramebufferState(minecraft.framebuffer), ViewportState(minecraft.framebuffer),
                BlendState(true), BlendFuncState(GL_ONE, GL_ONE_MINUS_SRC_ALPHA),
            ) {
                renderHudBlur()
            }
        } else {
            StateIsolation.isolate(FramebufferState(blurFramebuffer), ViewportState(blurFramebuffer)) {
                hudFramebuffer.draw(minecraft.window.framebufferWidth, minecraft.window.framebufferHeight, false)
            }
            StateIsolation.isolate(FramebufferState(minecraft.framebuffer), ViewportState(minecraft.framebuffer)) {
                blurFramebuffer.draw(minecraft.window.framebufferWidth, minecraft.window.framebufferHeight, false)
            }
        }

        BloomEffect.brightnessPassFramebuffer = blurFramebuffer
        BloomEffect.brightnessThreshold = 1F
        BloomEffect.renderBloom()

        StateIsolation.isolate(BlendFuncState(GL_ONE, GL_ONE)) {
            PostProcessRenderer.copyFramebuffer(BloomEffect.bloomUpFramebuffer, minecraft.framebuffer, false)
        }

        hudFramebuffer.clear(true)
        blurFramebuffer.clear(true)

        renderHud = false
        useBlur = false
        useBloom = false
    }

    private fun dropHudShadow() {
        GaussianBlurRenderer.gaussianKernel = GaussianBlurRenderer.generateSymmetricGaussianKernel(1.0F, maxSigma = 20F)
        BlurRenderer.renderGaussianBlur(hudFramebuffer, blurFramebuffer)

        StateIsolation.isolate(
            FramebufferState(blurFramebuffer),
            ViewportState(blurFramebuffer),
            BlendState(true),
            BlendFuncState(GL_ONE, GL_ONE_MINUS_SRC_ALPHA),
        ) {
            // Render the blurred hud background to blurFramebuffer
            hudFramebuffer opacityMask BlurRenderer.blurFramebuffer

            // Render half-transparent hud on the blurred hud background
            hudFramebuffer.draw(minecraft.window.framebufferWidth, minecraft.window.framebufferHeight, false)
        }
    }

    private fun renderHudBlur() {
        val strength = 1.0F - magicShownOpacityAnimation.animatedValue.toFloat()
        if (strength == .0F) {
            blurFramebuffer.draw(minecraft.window.framebufferWidth, minecraft.window.framebufferHeight, false)
            return
        }

        GaussianBlurRenderer.gaussianKernel = GaussianBlurRenderer.generateSymmetricGaussianKernel(strength, maxSigma = 20F)
        BlurRenderer.renderGaussianBlurFullResolution(blurFramebuffer)
        BlurRenderer.blurFramebuffer.draw(minecraft.window.framebufferWidth, minecraft.window.framebufferHeight, false)
    }

    private fun renderPoints() {
        val result = IntArray(1)
        glGenVertexArrays(result)

        val vertexArray = result[0]
        val vertexBuffer = glGenBuffers()

        glBindVertexArray(vertexArray)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)

        val buffer = MemoryUtil.memAllocFloat(points.size).put(points).flip()
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(buffer)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * 4, 0L)
        glEnableVertexAttribArray(0)

        pointSpriteShader.enableShader()
        glBindVertexArray(vertexArray)
        glEnable(GL_PROGRAM_POINT_SIZE)
        glDrawArrays(GL_POINTS, 0, points.size / 2)
        pointSpriteShader.disableShader()

        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glDisableVertexAttribArray(0)
        glDisable(GL_PROGRAM_POINT_SIZE)

        glDeleteBuffers(vertexBuffer)
        glDeleteVertexArrays(vertexArray)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glDisable(GL_BLEND)
    }

    private fun performChannelMagicAnimation() {
        val delta = Util.getMeasuringTimeNano() - lastNanos
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
        return MinecraftClient.getInstance().options.playerListKey.isPressed
    }

    private val isPressingTab
        get() = MinecraftClient.getInstance().options.playerListKey.isPressed

    @JvmField
    var isPressingRightMouseButton = false

    @JvmField
    var isPressingLeftMouseButton = false

    private fun shouldSlowTime(): Boolean {
        val minecraft = MinecraftClient.getInstance()
        val server = minecraft.server
        return minecraft.isIntegratedServerRunning && (server != null && !server.isRemote)
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
        dissolveAnimation.start()

        magicShownAnimationClock.start()
        magicShownOpacityAnimationClock.start()

        ManaCostTooltip.show()
        AimAssist.resetAnimation()
        if (firstShow) {
            firstShow = false
            selectTargetEntity(RenderTickCounter.ZERO)
            if (targetedEntity == null) {
                descriptionYOffsetAnimation.animatedValue = -35.0
            }
        }
    }

    private fun renderManaBar(
        drawContext: DrawContext,
        tickCounter: RenderTickCounter,
    ) {
        val channelSequence = player.getChannelSequence(
            targetedEntity
        )
        val magicCost = selectedMagic.getCost(player, targetedEntity, channelSequence).toDouble()
        manaBar.manaCost.value = magicCost

        val renderer = LegacyMatrixUIRenderer(
            drawContext.vertexConsumers
        )
        manaBar.render(
            drawContext, renderer
        )
    }

    private fun renderLeftPart(
        drawContext: DrawContext,
        tickCounter: RenderTickCounter,
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
        val textRenderer = MinecraftClient.getInstance().textRenderer
        var length = 0
        var index = 0
        for (character in string) {
            length += textRenderer.getWidth(
                character.toString()
            )
            if (length > width) {
                return string.substring(
                    0, index
                )
            }
            ++index
        }

        return string
    }

    private fun renderMagic(
        drawContext: DrawContext,
        tickCounter: RenderTickCounter,
        index: Int,
        magic: Magic,
        indentList: List<Double>,
    ) {
        val displayData = magicDisplayData[index - 1]
        val xIndent = indentList[index - 1].toInt()

        val animatedColor = magicColorAnimations.getOrNull(
            index - 1
        )
        val color = ColorHelper.Argb.getArgb(
            (magicShownOpacityAnimation.animatedValue * 127.5).toInt(), animatedColor?.red?.animatedValue?.toInt() ?: 0, animatedColor?.green?.animatedValue?.toInt() ?: 0, animatedColor?.blue?.animatedValue?.toInt() ?: 0
        )

        val height = 20.0
        val margin = 5.0

        val startY = (index * (height + margin) + drawContext.scaledWindowHeight / 2 - (indentList.size + 1) * (height + margin) / 2).toInt()
        val endY = (startY + height).toInt()

        val status = getMagicAvailableStatus(
            magic
        ).let {
            if (it == TARGET_MISSING) {
                AVAILABLE
            } else {
                it
            }
        }

        val channelSequence = player.getChannelSequence(
            targetedEntity
        )
        val normalCost = magic.getNormalCost()
        val cost = magic.getCost(player, targetedEntity, channelSequence)
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

        val textRenderer = MinecraftClient.getInstance().textRenderer
        displayData.costWidthAnimation.value = textRenderer.getWidth(costString).toDouble()

        val magicNameWidth = textRenderer.getWidth(magic.name)
        val costStringWidth = displayData.costWidthAnimation.animatedValue
        val statusStringWidth = textRenderer.getWidth(status.description)
        val extraWidth = magicNameWidth + costStringWidth + statusStringWidth + 30 /* padding */
        val extraWidthAnimation = magicExtraWidthAnimations[index - 1].animation
        extraWidthAnimation.currentValue = extraWidth

        drawContext.enableScissor(
            xIndent + 50 + magicShownAnimation.animatedValue.toInt(), startY, xIndent + 50 + extraWidthAnimation.animatedValue.toInt() + magicShownAnimation.animatedValue.toInt(), endY
        )

        val transformationMatrix = drawContext.matrices.peek().positionMatrix
        val tessellator = Tessellator.getInstance()

        // BlurRenderer.blurTextureRenderShader.enableShader()
        // if (magicShownOpacityAnimation.animatedValue != .0) {
        //     BlurRenderer.renderQuad()
        // }
        // BlurRenderer.blurTextureRenderShader.disableShader()

        val blurBackgroundStartX = xIndent + 50 + magicShownAnimation.animatedValue.toInt()
        val blurBackgroundEndX = xIndent + 50 + extraWidthAnimation.animatedValue.toInt() + magicShownAnimation.animatedValue.toInt()

        val builder = Tessellator.getInstance()
        // var buffer = builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
        // buffer.vertex(blurBackgroundStartX.toFloat(), endY.toFloat(), 0F).color(color).texture(0F, 0F)
        // buffer.vertex(blurBackgroundEndX.toFloat(), endY.toFloat(), 0F).color(color).texture(1F, 0F)
        // buffer.vertex(blurBackgroundEndX.toFloat(), startY.toFloat(), 0F).color(color).texture(1F, 1F)
        // buffer.vertex(blurBackgroundStartX.toFloat(), startY.toFloat(), 0F).color(color).texture(0F, 1F)
//
        // RenderSystem.enableBlend()
        // dissolveShader.dissolveFactor = dissolveAnimation.animatedValue.toFloat()
        // dissolveShader.enableShader()
        // BufferRenderer.draw(buffer.end())
        // dissolveShader.disableShader()
        // RenderSystem.disableBlend()

        var buffer = builder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR)
        buffer.vertex(
            transformationMatrix, blurBackgroundEndX.toFloat(), endY.toFloat(), 0f
        ).color(
            color
        ).texture(
            1.0F, 1.0F
        )
        buffer.vertex(
            transformationMatrix, blurBackgroundEndX.toFloat(), startY.toFloat(), 0f
        ).color(
            color
        ).texture(
            1.0F, .0F
        )
        buffer.vertex(
            transformationMatrix, blurBackgroundStartX.toFloat(), startY.toFloat(), 0f
        ).color(
            color
        ).texture(
            .0F, .0F
        )
        buffer.vertex(
            transformationMatrix, blurBackgroundStartX.toFloat(), endY.toFloat(), 0f
        ).color(
            color
        ).texture(
            .0F, 1.0F
        )

        // drawContext.drawTexture()
        RenderSystem.enableBlend()
        // RenderSystem.setShaderTexture(0, Identifier.of("textures/item/diamond_sword"))
        // RenderSystem.setShaderTexture(0, MinecraftClient.getInstance().framebuffer.colorAttachment)
        BufferRenderer.draw(
            buffer.end()
        )

        // MinecraftClient.getInstance().framebuffer.beginWrite(false)
        // BlurRenderer.blurFramebuffer.beginRead()
        // GL30.glBlitFramebuffer(
        //     blurBackgroundEndX,
        //     endY,
        //     blurBackgroundStartX,
        //     startY,
        //     blurBackgroundEndX,
        //     endY,
        //     blurBackgroundStartX,
        //     startY,
        //     GL30.GL_COLOR_BUFFER_BIT,
        //     GL30.GL_NEAREST
        // )
        // BlurRenderer.blurFramebuffer.endRead()
        //  BlurRenderer.blurFramebuffer.beginWrite(true)

        drawContext.fill(
            xIndent + 50 + magicShownAnimation.animatedValue.toInt(), startY, xIndent + 50 + extraWidthAnimation.animatedValue.toInt() + magicShownAnimation.animatedValue.toInt(), endY, 0, color
        )

        val alpha = magicShownOpacityAnimation.animatedValue * 255
        val foregroundColor = ColorHelper.Argb.getArgb(alpha.toInt(), 255, 255, 255)
        if (alpha > 4) {
            drawContext.drawText(textRenderer, magic.name, xIndent + 55 + magicShownAnimation.animatedValue.toInt(), startY + 5, foregroundColor, false)
        }

        val costAlpha = min(alpha, displayData.costChangedAnimation.animatedValue * 255)
        // println((displayData.costChangedAnimation.animatedValue * 255).toInt())
        if (costAlpha > 4) {
            val costForegroundColor = ColorHelper.Argb.getArgb(costAlpha.toInt(), 255, 255, 255)
            drawContext.drawText(textRenderer, Text.literal(costString), xIndent + 65 + magicNameWidth + magicShownAnimation.animatedValue.toInt(), startY + 5, costForegroundColor, false)
        }

        val statusAlpha = min(alpha, displayData.statusChangedAnimation.animatedValue * 255)
        if (statusAlpha > 4) {
            val statusForegroundColor = ColorHelper.Argb.getArgb(statusAlpha.toInt(), 255, 255, 255)
            drawContext.drawText(textRenderer, statusString, xIndent + 75 + magicNameWidth + costStringWidth.toInt() + magicShownAnimation.animatedValue.toInt(), startY + 5, statusForegroundColor, false)
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
            val progressColor = Color(
                25, 192, 25, (magicShownOpacityAnimation.animatedValue * 128).toInt()
            )
            val brightProgressColor = Color(
                25, 255, 25, (magicShownOpacityAnimation.animatedValue * 255).toInt().coerceAtMost(
                    255
                )
            )
            val transparentColor = Color(
                0, 0, 0, 0
            )

            RenderSystem.enableBlend()
            RenderSystem.setShader(GameRenderer::getPositionColorProgram)

            // We're drawing in triangle strip mode, the first 3 vertices form the first triangle.
            // Each added vertex forms a new triangle with the first vertex and the last vertex.
            //
            // We need to draw a parallelogram, we split the parallelogram into two triangles to render
            // in this mode, first we draw the left triangle by the following order:
            // 1. lower left corner (startX, endY)
            // 2. lower right corner (endX, endY)
            // 3. upper right corner (endX, startY)
            // Then we draw the right triangle, in this mode we only need to draw a new vertex that will
            // form a new triangle with the first two vertices, so we only need to draw the upper right
            // of the right triangle (endX + width, startY)
            drawContext.enableScissor(
                xIndent + 50 + magicShownAnimation.animatedValue.toInt(), startY, xIndent + 50 + extraWidthAnimation.animatedValue.toInt() + magicShownAnimation.animatedValue.toInt(), endY
            )

            val halfTransparentColor = Color(
                0, 255, 0, (magicShownOpacityAnimation.animatedValue * 255 * (1.0 - animationProgress)).toInt()
            )
            buffer = tessellator.begin(
                VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR
            )
            buffer.vertex(
                transformationMatrix, startX.toFloat(), endY.toFloat(), 0f
            ).color(
                transparentColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, endX.toFloat(), endY.toFloat(), 0f
            ).color(
                halfTransparentColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, endX.toFloat(), startY.toFloat(), 0f
            ).color(
                transparentColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, (endX + height).toFloat(), startY.toFloat(), 0f
            ).color(
                halfTransparentColor.toInt()
            )
            BufferRenderer.drawWithGlobalProgram(buffer.end())

            val multiplier = 4.0F
            RenderSystem.setShaderColor(multiplier, multiplier, multiplier, (0.7F - animationProgress).toFloat())
            buffer = tessellator.begin(
                VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR
            )
            buffer.vertex(
                transformationMatrix, startX.toFloat(), endY.toFloat(), 0f
            ).color(
                progressColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, endX.toFloat(), endY.toFloat(), 0f
            ).color(
                progressColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, endX.toFloat(), startY.toFloat(), 0f
            ).color(
                progressColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, (endX + height).toFloat(), startY.toFloat(), 0f
            ).color(
                progressColor.toInt()
            )
            BufferRenderer.drawWithGlobalProgram(
                buffer.end()
            )

            buffer = tessellator.begin(
                VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR
            )
            buffer.vertex(
                transformationMatrix, (startX + 20).toFloat(), endY.toFloat(), 0f
            ).color(
                progressColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, (endX + 20).toFloat(), endY.toFloat(), 0f
            ).color(
                progressColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, (endX + 20).toFloat(), startY.toFloat(), 0f
            ).color(
                progressColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, (endX + 20 + height).toFloat(), startY.toFloat(), 0f
            ).color(
                progressColor.toInt()
            )
            BufferRenderer.drawWithGlobalProgram(
                buffer.end()
            )

            buffer = tessellator.begin(
                VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR
            )
            buffer.vertex(
                transformationMatrix, (startX + 10).toFloat(), endY.toFloat(), 0f
            ).color(
                brightProgressColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, (endX + 10).toFloat(), endY.toFloat(), 0f
            ).color(
                brightProgressColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, (endX + 10).toFloat(), startY.toFloat(), 0f
            ).color(
                brightProgressColor.toInt()
            )
            buffer.vertex(
                transformationMatrix, (endX + 10 + height).toFloat(), startY.toFloat(), 0f
            ).color(
                brightProgressColor.toInt()
            )

            RenderSystem.enableBlend()
            BufferRenderer.drawWithGlobalProgram(buffer.end())
            RenderSystem.defaultBlendFunc()
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

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

    private fun renderOverclock(
        drawContext: DrawContext,
        tickCounter: RenderTickCounter,
    ) {
        val renderer = LegacyMatrixUIRenderer(
            drawContext.vertexConsumers
        )
        val manaOverclockMinPoint = Point(
            renderer.scaledWindowWidth - 5.0, renderer.scaledWindowHeight.toDouble() - 25.0
        )
        val manaOverclockMaxPoint = Point(
            renderer.scaledWindowWidth - 10.0, MathHelper.lerp(
                manaOverclock.animatedValue / 10, renderer.scaledWindowHeight.toDouble() - 25.0, 25.0
            )
        )

        val magicOverclockMinPoint = Point(
            renderer.scaledWindowWidth - 15.0, renderer.scaledWindowHeight.toDouble() - 25.0
        )

        val magicOverclockMaxPoint = Point(
            renderer.scaledWindowWidth - 20.0, MathHelper.lerp(
                magicOverclock.animatedValue / 10, renderer.scaledWindowHeight.toDouble() - 25.0, 25.0
            )
        )

        val magicOverclockColor = Color(
            MathHelper.lerp(
                magicOverclock.animatedValue / 10.0, 0.0, 255.0
            ).toInt(), MathHelper.lerp(
                magicOverclock.animatedValue / 10.0, 255.0, 0.0
            ).toInt(), 0, 128
        )

        val manaOverclockColor = Color(
            MathHelper.lerp(
                manaOverclock.animatedValue / 10.0, 0.0, 255.0
            ).toInt(), MathHelper.lerp(
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

    private fun selectTargetEntity(tickCounter: RenderTickCounter) {
        var targetedEntity = getTargetedEntity(tickCounter.getTickDelta(true))
        if (targetedEntity is EnderDragonPart) {
            targetedEntity = targetedEntity.owner
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

    private fun renderRightPart(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val backgroundColor = ColorHelper.Argb.getArgb((magicShownOpacityAnimation.animatedValue * 127.5).toInt(), 255, 255, 255)
        val alpha = (magicShownOpacityAnimation.animatedValue * 255).coerceIn(.0..255.0).toInt()

        val builder = Tessellator.getInstance()
        val buffer = builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
        val offsetX = magicShownAnimation.animatedValue.toFloat()
        val offsetY = descriptionExtraHeightAnimation.animatedValue.toFloat()

        val windowWidth = drawContext.scaledWindowWidth
        val windowHeight = drawContext.scaledWindowHeight

        val leftX = windowWidth - 200 - offsetX
        val rightX = windowWidth - 25 - offsetX
        val topY = windowHeight / 2 - 100F - offsetY / 2F
        val bottomY = windowHeight / 2 + 100F + offsetY / 2F

        val width = rightX - leftX
        val height = bottomY - topY

        buffer.vertex(rightX, topY, 0F).texture(0F, 0F).color(backgroundColor)
        buffer.vertex(leftX, topY, 0F).texture(1F, 0F).color(backgroundColor)
        buffer.vertex(leftX, bottomY, 0F).texture(1F, 1F).color(backgroundColor)
        buffer.vertex(rightX, bottomY, 0F).texture(0F, 1F).color(backgroundColor)

        dissolveShader.dissolveFactor = dissolveAnimation.animatedValue.toFloat()
        dissolveShader.resolutionX = width
        dissolveShader.resolutionY = height
        dissolveShader.enableShader()

        StateIsolation.isolate(BlendState(true)) {
            BufferRenderer.draw(buffer.end())
        }

        dissolveShader.disableShader()
        dissolveShader.resolutionX = 1.0F
        dissolveShader.resolutionY = 1.0F

        val descriptionAlpha = min(magicShownOpacityAnimation.animatedValue * 255, entityDescriptionOpacityAnimation.animatedValue * 255).toInt()
        val cachedTargetedEntity = this.cachedTargetedEntity

        val textRenderer = MinecraftClient.getInstance().textRenderer
        if (cachedTargetedEntity != null) {
            val red = ColorHelper.Argb.getArgb(descriptionAlpha, 255, 25, 25)
            val green = ColorHelper.Argb.getArgb(descriptionAlpha, 25, 255, 25)

            val health = cachedTargetedEntity.health.toDouble()
            val maxHealth = cachedTargetedEntity.maxHealth.toDouble()

            if (healthAnimation.from.isNaN()) {
                healthAnimation.from = .0
            }
            if (maxHealthAnimation.from.isNaN()) {
                maxHealthAnimation.from = .0
            }
            if (healthPercentageAnimation.from.isNaN()) {
                healthPercentageAnimation.from = .0
            }

            healthAnimation.value = health
            maxHealthAnimation.value = maxHealth
            healthPercentageAnimation.value = health / maxHealth

            val percentage = healthPercentageAnimation.animatedValue
            val lerpedColor = ColorHelper.Argb.lerp(percentage.toFloat(), red, green)

            val sizeReduced = if (sizeScalingAnimation) {
                10 - (entityDescriptionOpacityAnimation.animatedValue * 10).toInt()
            } else {
                0
            }

            val progressBarX = MathHelper.lerp(percentage.toFloat(), 190, 35)
            val x1 = drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt() + sizeReduced
            val x2 = drawContext.scaledWindowWidth - progressBarX - magicShownAnimation.animatedValue.toInt() - sizeReduced

            val multiplier = 1.0F
            RenderSystem.setShaderColor(multiplier, multiplier, multiplier, 1.0F)
            drawContext.fill(
                x1.coerceAtMost(x2),
                drawContext.scaledWindowHeight / 2 - 80 - (descriptionExtraHeightAnimation.animatedValue / 2).toInt() - sizeReduced,
                x2,
                drawContext.scaledWindowHeight / 2 - 75 - (descriptionExtraHeightAnimation.animatedValue / 2).toInt() - sizeReduced,
                lerpedColor
            )
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

            val fontSizeReduced = if (sizeScalingAnimation) {
                entityDescriptionOpacityAnimation.animatedValue.toFloat()
            } else {
                1F
            }
            val descriptionForegroundColor = ColorHelper.Argb.getArgb(descriptionAlpha, 255, 255, 255)
            if (descriptionAlpha > 3) {
                val healthText = StringBuilder()
                    .append((healthAnimation.animatedValue * 100).toLong().toDouble() / 100)
                    .append("/")
                    .append((maxHealthAnimation.animatedValue * 100).toLong().toDouble() / 100)
                    .toString()

                val textX = drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt()
                val textY = drawContext.scaledWindowHeight / 2 - 70 - (descriptionExtraHeightAnimation.animatedValue / 2).toInt()

                val textWidth = textRenderer.getWidth(healthText)
                val textCenterX = textX + textWidth / 2.0
                val textCenterY = textY + textRenderer.fontHeight / 2.0

                drawContext.matrices.push()

                drawContext.matrices.translate(textCenterX, textCenterY, 0.0)
                drawContext.matrices.scale(fontSizeReduced, fontSizeReduced, fontSizeReduced)
                drawContext.matrices.translate(-textCenterX, -textCenterY, 0.0)

                drawContext.drawText(
                    textRenderer,
                    healthText,
                    textX,
                    textY,
                    descriptionForegroundColor,
                    false
                )
                drawContext.matrices.pop()
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
            val entityNameForegroundColor = ColorHelper.Argb.getArgb(entityNameAlpha, 255, 255, 255)
            if (entityNameAlpha > 3) {
                val textX = drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt()
                val textY = drawContext.scaledWindowHeight / 2 - 90 - (descriptionExtraHeightAnimation.animatedValue / 2).toInt()

                val textWidth = textRenderer.getWidth(displayEntityName)
                val textCenterX = textX + textWidth / 2.0
                val textCenterY = textY + textRenderer.fontHeight / 2.0

                drawContext.matrices.push()

                drawContext.matrices.translate(textCenterX, textCenterY, 0.0)
                drawContext.matrices.scale(fontSizeReduced, fontSizeReduced, fontSizeReduced)
                drawContext.matrices.translate(-textCenterX, -textCenterY, 0.0)

                drawContext.drawText(textRenderer, displayEntityName, textX, textY, entityNameForegroundColor, true)

                drawContext.matrices.pop()
            }
        }

        if (alpha <= 3) {
            return
        }

        drawContext.enableScissor(
            drawContext.scaledWindowWidth - 200 - magicShownAnimation.animatedValue.toInt(),
            drawContext.scaledWindowHeight / 2 - 100 - (descriptionExtraHeightAnimation.animatedValue / 2).toInt(),
            drawContext.scaledWindowWidth - 25 - magicShownAnimation.animatedValue.toInt(),
            drawContext.scaledWindowHeight / 2 + 100 + (descriptionExtraHeightAnimation.animatedValue / 2).toInt()
        )
        val currentMagic = MatrixClient.getPlayerMagics()[selectedIndex - 1]

        if (currentMagic.description != currentDescription) {
            currentDescription = currentMagic.description
            magicDescriptionChangedAnimation.value = .0
        }
        if (magicDescriptionChangedAnimation.animatedValue == .0) {
            magicDescriptionChangedAnimation.value = 1.0
            displayDescription = currentDescription
        }

        val descriptionY = drawContext.scaledWindowHeight / 2 - 55 + descriptionYOffsetAnimation.animatedValue.toInt() - (descriptionExtraHeightAnimation.animatedValue / 2).toInt() // + magicSwitchAnimation.animatedValue

        val lines = textRenderer.wrapLines(currentDescription, 150)
        val extraHeight = textRenderer.fontHeight * lines.size - 180.0
        descriptionExtraHeightAnimation.value = extraHeight.coerceAtLeast(.0)

        val magicDescriptionAlpha = (min(magicShownOpacityAnimation.animatedValue, magicDescriptionChangedAnimation.animatedValue) * 255).toInt()
        if (magicDescriptionAlpha > 3) {
            drawContext.drawTextWrapped(textRenderer, displayDescription, drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt(), descriptionY, 150, ColorHelper.Argb.getArgb(magicDescriptionAlpha, 255, 255, 255))
        }
        drawContext.disableScissor()
    }

    private fun getTargetedEntity(tickDelta: Float): Entity? {
        val candidateEntities = getAssistTargetEntity(tickDelta)//.filter { entity ->
        //    val rotationVector = player.getRotationVec(tickDelta)
        //    val from = player.getCameraPosVec(tickDelta)
        //    val to = player.getCameraPosVec(tickDelta).add(rotationVector.multiply(aimAssistMaxDistance))
        //    val blockHit = player.world.raycast(RaycastContext(from, to, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.ANY, player))
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
        )
        if (entities == null) {
            return emptyList()
        }

        return entities
            .filter { it is LivingEntity && !it.isSpectator && it.isAlive }
            .map { it as LivingEntity }
            .filter {
                val channelSequence = player.getChannelSequence(it)
                selectedMagic.availableStatus(player, it, channelSequence) == AVAILABLE
            }
    }

    private fun getRayCastTargetEntity(tickDelta: Float): Entity? {
        val range = 10000.0

        val minecraftClient = MinecraftClient.getInstance()!!
        val camera = minecraftClient.cameraEntity!!

        val location = camera.getCameraPosVec(tickDelta)
        val rotation = camera.getRotationVec(tickDelta)
        val min = location.add(rotation)
        val max = location.add(rotation.multiply(range))
        val box = Box(min, max)

        // Wrap Dancer: Select entities through walls
        if (player.wizardHelmet.item is WizardHelmet5) {
            val result = ProjectileUtil.raycast(camera, min, max, box, { entity ->
                if (entity.isSpectator) {
                    return@raycast false
                }
                true
            }, range)
            return result?.entity
        }

        val blockHit = player.world.raycast(RaycastContext(min, max, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.ANY, player))
        val blockHitDistance = player.squaredDistanceTo(blockHit.pos)
        val result = ProjectileUtil.raycast(camera, min, max, box, { entity ->
            if (entity.isSpectator) {
                return@raycast false
            }

            blockHitDistance > entity.squaredDistanceTo(player)
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
            val tickDelta = minecraft.renderTickCounter.getTickDelta(true)
            val lerpedPosition = targetedEntity.getLerpedPos(tickDelta).add(
                .0,
                targetedEntity.boundingBox.lengthY / 2,
                .0
            )
            val screenPosition = worldToScreen(lerpedPosition)
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
        if (window != minecraft.window.handle) {
            return false
        }

        if (isPressingTab && InputUtil.isKeyPressed(minecraft.window.handle, GLFW.GLFW_KEY_E) &&
            player.getEquippedStack(EquipmentSlot.CHEST).item is LightningChestplate1
        ) {
            ClientPlayNetworking.send(BorrowedTimePayload())
            return true
        }

        if (InputUtil.isKeyPressed(minecraft.window.handle, GLFW.GLFW_KEY_E) && shouldRenderHud()) {
            ClientPlayNetworking.send(ActiveBloodPactPayload())
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
        if (window != minecraft.window.handle) {
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