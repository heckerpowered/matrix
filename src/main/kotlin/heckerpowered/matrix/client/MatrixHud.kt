package heckerpowered.matrix.client

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.core.AimAssist
import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.LegacyMatrixUIRenderer
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.render.Rectangle
import heckerpowered.matrix.client.shader.DissolveShader
import heckerpowered.matrix.client.shader.UIBlurShader
import heckerpowered.matrix.client.ui.element.AvailableStatusTooltip
import heckerpowered.matrix.client.ui.element.ManaBar
import heckerpowered.matrix.client.ui.element.SystemCrashBar
import heckerpowered.matrix.client.ui.foundation.animation.*
import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.magics.MagicAvailableStatus
import heckerpowered.matrix.common.magics.MagicAvailableStatus.*
import heckerpowered.matrix.common.magics.description
import heckerpowered.matrix.common.network.OverclockPayload
import heckerpowered.matrix.common.network.UseMagicPayload
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.getChannelSequence
import heckerpowered.matrix.core.lerp
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.entity.Entity
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
import java.time.Duration
import kotlin.math.min

object MatrixHud {
    var mana
        get() = manaBar.mana.currentValue
        set(value) {
            manaBar.mana.currentValue = value.coerceIn(0.0..maxMana)
        }

    var maxMana
        get() = manaBar.maxMana.currentValue.coerceAtLeast(
            .0
        )
        set(value) {
            manaBar.maxMana.currentValue = value
        }

    var manaUsage
        get() = manaBar.manaUsage
        set(value) {
            manaBar.manaUsage = value
        }

    var targetedEntity: LivingEntity? = null
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
    private val timeSlowAnimationClock = AnimationClock(
        Duration.ofMillis(
            300
        ), 1.0, 0.01
    )
    private val timeSlowAnimation = DoubleAnimation(
        timeSlowAnimationClock, easingFunction
    )

    private val currentHealthAnimationClock = AnimationClock(
        Duration.ofMillis(
            300
        ), 1.0, 1.0
    )
    private val currentHealthAnimation = DoubleAnimation(
        currentHealthAnimationClock, easingFunction
    )
    private val maxHealthAnimationClock = AnimationClock(
        Duration.ofMillis(
            300
        ), 1.0, 1.0
    )
    private val maxHealthAnimation = DoubleAnimation(
        maxHealthAnimationClock, easingFunction
    )

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
    private val magicDescriptionPrimaryOpacityAnimation = SimpleDoubleAnimation(from = 1.0, to = 1.0, duration = Duration.ofMillis(150))
    private val magicDescriptionSecondaryOpacityAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(150))
    private val descriptionYOffsetAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(300))

    private val entityDescriptionOpacityAnimation = SimpleDoubleAnimation()
    private var previousVisibility = false

    private var aimEntity: Entity? = null
    private var useAimAssist = false
    private var firstShow = true

    init {
        easingFunction.easingMode = EasingMode.OUT
        easingFunction.oscillations = 0

        magicOverclock.currentValue = 1.0
        manaOverclock.currentValue = 1.0

        entityDescriptionOpacityAnimation.start()
        descriptionYOffsetAnimation.start()
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
            ClientPlayNetworking.send(
                OverclockPayload(
                    manaOverclock.currentValue, magicOverclock.currentValue
                )
            )
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
        HudRenderCallback.EVENT.register(
            this::onHudRender
        )
        SystemCrashBar.onInitialize()
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
        val magic = selectedMagic
        val target = this.targetedEntity
        val channelSequence = player.getChannelSequence(
            target
        )
        manaBar.manaCost = magic.getCost(player, target, channelSequence).toDouble()

        magicDescriptionPrimaryOpacityAnimation.startTime = Duration.ofMillis(150)
        magicDescriptionPrimaryOpacityAnimation.from = .0
        magicDescriptionPrimaryOpacityAnimation.to = 1.0
        magicDescriptionPrimaryOpacityAnimation.start()

        magicDescriptionSecondaryOpacityAnimation.from = 1.0
        magicDescriptionSecondaryOpacityAnimation.to = .0
        magicDescriptionSecondaryOpacityAnimation.start()
    }

    private val selectedMagic: Magic
        get() = MatrixClient.getPlayerMagics()[selectedIndex - 1]

    private fun useCurrentMagic() {
        if (!shouldRenderHud()) {
            return
        }

        useMagicIndexed(
            selectedIndex - 1
        )
    }

    private fun useMagicIndexed(
        index: Int,
    ) {
        val magic = MatrixClient.getPlayerMagics()[index]
        val target = this.targetedEntity ?: return
        if (magic.availableStatus(
                player, target, player.getChannelSequence(
                    target
                )
            ) != AVAILABLE
        ) {
            return
        }

        magicColorAnimations[index].setColorWithoutAnimation(
            .0, 255.0, .0
        )

        // Reset use magic animation
        usingMagicList[index + 1] = 0.0

        channelMagic(
            magic, target
        )

        minecraft.world!!.playSound(
            player, player.x, player.y, player.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1f
        )
    }

    private fun channelMagic(
        magic: Magic,
        target: LivingEntity,
    ) {
        ClientPlayNetworking.send(
            UseMagicPayload(
                magic.id, target.id
            )
        )
        ChannelSequence.channelMagic(
            magic, player, target
        )
        ChannelSequence.channelMagicClient(
            magic, target
        )
    }

    private fun checkVisibilityChanges() {
        val shouldRenderHud = shouldRenderHud()
        if (shouldRenderHud != previousVisibility) {
            onHudVisibilityChanged(shouldRenderHud)
        }
        previousVisibility = shouldRenderHud
    }

    private fun wrapDancer() {
        if (!shouldSlowTime()) {
            return
        }

        if (timeSlowAnimation.animatedValue != 1.0 && timeSlowAnimation.animatedValue != 0.01) {
            Wrap.setClientTimeScale(
                timeSlowAnimation.animatedValue
            )
        }
    }

    private fun checkMagicAvailableStatus() {
        val magics = MatrixClient.getPlayerMagics()
        fillColorAnimationList(
            magics
        )

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
                AVAILABLE_MANA_NOT_ENOUGH -> magicColorAnimations[index].setColor(
                    128.0, 0.0, 0.0
                )

                TARGET_IMMUNE -> magicColorAnimations[index].setColor(
                    128.0, 0.0, 0.0
                )

                UNAVAILABLE -> magicColorAnimations[index].setColor(
                    128.0, 0.0, 0.0
                )

                CHANNEL_QUEUE_FULL -> magicColorAnimations[index].setColor(
                    128.0, 0.0, 0.0
                )

                CHANNEL_QUEUE_LOCKED -> magicColorAnimations[index].setColor(
                    128.0, 0.0, 0.0
                )

                TARGET_MISSING -> magicColorAnimations[index].setColor(
                    .0, .0, .0
                )

                AVAILABLE -> magicColorAnimations[index].setColor(
                    .0, .0, .0
                )
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

    private fun onHudRender(
        drawContext: DrawContext,
        tickCounter: RenderTickCounter,
    ) {
        val renderer = LegacyMatrixUIRenderer(
            drawContext.vertexConsumers
        )

        checkVisibilityChanges()
        wrapDancer()
        processKeyInput()
        checkMagicAvailableStatus()

        performChannelMagicAnimation()
        lastNanos = Util.getMeasuringTimeNano()

        if (magicShownOpacityAnimation.animatedValue == .0) {
            for (animation in magicExtraWidthAnimations) {
                animation.animation.currentValue = .0
            }
            return
        }
        UIBlurShader.renderBlur()
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
        // BlurRenderer.renderBlur(drawContext, tickCounter)
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
    fun shouldRenderHud() = MinecraftClient.getInstance().options.playerListKey.isPressed

    private fun shouldSlowTime(): Boolean {
        val minecraft = MinecraftClient.getInstance()
        val server = minecraft.server
        return minecraft.isIntegratedServerRunning && (server != null && !server.isRemote)
    }

    private fun onHudVisibilityChanged(
        visibility: Boolean,
    ) {
        manaBar.onHudVisibilityChanged(
            visibility
        )
        if (visibility) {
            onHudShown()
        } else {
            onHudHide()
        }
    }

    private fun onHudHide() {
        if (shouldSlowTime()) {
            Wrap.setTimeScale(
                1.0
            )

            timeSlowAnimationClock.let {
                it.from = timeSlowAnimation.animatedValue
                it.to = 1.0
            }

            timeSlowAnimationClock.start()
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
    }

    private fun onHudShown() {
        if (shouldSlowTime()) {
            Wrap.setTimeScale(
                0.01
            )

            timeSlowAnimationClock.let {
                it.from = timeSlowAnimation.animatedValue
                it.to = 0.01
            }

            timeSlowAnimationClock.start()
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
        val magicCost = selectedMagic.getCost(
            player, targetedEntity, channelSequence
        ).toDouble()
        if (manaBar.manaCost != magicCost) {
            manaBar.manaCost = magicCost
        }

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
        val indentList = generateIndentList(
            magics.size
        )
        fillExtraWidthAnimationList(
            magics
        )
        repeat(
            magics.size
        ) {
            renderMagic(
                drawContext, tickCounter, it + 1, magics[it], indentList
            )
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
        indentList: List<Int>,
    ) {
        val xIndent = indentList[index - 1]

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
        val costString = magic.getCost(
            player, targetedEntity, channelSequence
        ).toString()
        val statusString = status.description

        val textRenderer = MinecraftClient.getInstance().textRenderer
        val magicNameWidth = textRenderer.getWidth(
            magic.name
        )
        val costStringWidth = textRenderer.getWidth(
            costString
        )
        val statusStringWidth = textRenderer.getWidth(
            statusString
        )
        val extraWidth = magicNameWidth + costStringWidth + statusStringWidth + 30 /* padding */
        val extraWidthAnimation = magicExtraWidthAnimations[index - 1].animation
        extraWidthAnimation.currentValue = extraWidth.toDouble()

        drawContext.enableScissor(
            xIndent + 50 + magicShownAnimation.animatedValue.toInt(), startY, xIndent + 50 + extraWidthAnimation.animatedValue.toInt() + magicShownAnimation.animatedValue.toInt(), endY
        )

        val transformationMatrix = drawContext.matrices.peek().positionMatrix
        val tessellator = Tessellator.getInstance()

        UIBlurShader.blurTextureRenderShader.enableShader()
        if (magicShownOpacityAnimation.animatedValue != .0) {
            UIBlurShader.renderQuad()
        }
        UIBlurShader.blurTextureRenderShader.disableShader()

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
        UIBlurShader.blurTextureRenderShader.disableShader()

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
        val foregroundColor = ColorHelper.Argb.getArgb(
            alpha.toInt(), 255, 255, 255
        )
        if (alpha > 5) {
            drawContext.drawText(
                textRenderer, magic.name, xIndent + 55 + magicShownAnimation.animatedValue.toInt(), startY + 5, foregroundColor, false
            )

            drawContext.drawText(
                textRenderer, Text.literal(
                    costString
                ), xIndent + 65 + magicNameWidth + magicShownAnimation.animatedValue.toInt(), startY + 5, foregroundColor, false
            )

            // val statusStringMaxWidth = extraWidthAnimation.animatedValue - magicNameWidth - costStringWidth - 20
            // val restrictedStatusString = if (extraWidthAnimation.animatedValue == extraWidth.toDouble()) {
            //     statusString.string
            // } else {
            //     restrictedSizedString(statusString.string, statusStringMaxWidth)
            // }

            drawContext.drawText(
                textRenderer, statusString, xIndent + 75 + magicNameWidth + costStringWidth + magicShownAnimation.animatedValue.toInt(), startY + 5, foregroundColor, false
            )
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
                0, 192, 0, (magicShownOpacityAnimation.animatedValue * 128).toInt()
            )
            val brightProgressColor = Color(
                0, 255, 0, (magicShownOpacityAnimation.animatedValue * 255).toInt().coerceAtMost(
                    255
                )
            )
            val transparentColor = Color(
                0, 0, 0, 0
            )

            RenderSystem.enableBlend()
            RenderSystem.setShader(
                GameRenderer::getPositionColorProgram
            )

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
            BufferRenderer.drawWithGlobalProgram(
                buffer.end()
            )

            RenderSystem.setShaderColor(
                1.0F, 1.0F, 1.0F, (0.7F - animationProgress).toFloat()
            )
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
            BufferRenderer.drawWithGlobalProgram(
                buffer.end()
            )

            RenderSystem.setShaderColor(
                1.0F, 1.0F, 1.0F, 1.0F
            )


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
        val aimEntity = this.aimEntity
        if (aimEntity is LivingEntity) {
            if (!aimEntity.isAlive) {
                this.aimEntity = null
            } else if (useAimAssist) {
                targetedEntity = aimEntity
                return
            }
        }
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

    private fun renderRightPart(
        drawContext: DrawContext,
        tickCounter: RenderTickCounter,
    ) {
        val backgroundColor = ColorHelper.Argb.getArgb(
            (magicShownOpacityAnimation.animatedValue * 127.5).toInt(), 0, 0, 0
        )
        val alpha = (magicShownOpacityAnimation.animatedValue * 255).coerceIn(
            .0..255.0
        ).toInt()
        val foregroundColor = ColorHelper.Argb.getArgb(alpha, 255, 255, 255)

        drawContext.enableScissor(
            drawContext.scaledWindowWidth - 200 - magicShownAnimation.animatedValue.toInt(), drawContext.scaledWindowHeight / 2 - 100, drawContext.scaledWindowWidth - 25 - magicShownAnimation.animatedValue.toInt(), drawContext.scaledWindowHeight / 2 + 100
        )
        UIBlurShader.blurTextureRenderShader.enableShader()
        UIBlurShader.renderQuad()
        UIBlurShader.blurTextureRenderShader.disableShader()
        drawContext.disableScissor()

        val builder = Tessellator.getInstance()
        val buffer = builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
        buffer.vertex(drawContext.scaledWindowWidth - 25 - magicShownAnimation.animatedValue.toFloat(), drawContext.scaledWindowHeight / 2 - 100F, 0F).texture(0F, 0F).color(backgroundColor)
        buffer.vertex(drawContext.scaledWindowWidth - 200 - magicShownAnimation.animatedValue.toFloat(), drawContext.scaledWindowHeight / 2 - 100F, 0F).texture(1F, 0F).color(backgroundColor)
        buffer.vertex(drawContext.scaledWindowWidth - 200 - magicShownAnimation.animatedValue.toFloat(), drawContext.scaledWindowHeight / 2 + 100F, 0F).texture(1F, 1F).color(backgroundColor)
        buffer.vertex(drawContext.scaledWindowWidth - 25 - magicShownAnimation.animatedValue.toFloat(), drawContext.scaledWindowHeight / 2 + 100F, 0F).texture(0F, 1F).color(backgroundColor)

        RenderSystem.enableBlend()
        dissolveShader.dissolveFactor = dissolveAnimation.animatedValue.toFloat()
        dissolveShader.enableShader()
        BufferRenderer.draw(buffer.end())
        dissolveShader.disableShader()
        RenderSystem.disableBlend()

        val descriptionAlpha = min(magicShownOpacityAnimation.animatedValue * 255, entityDescriptionOpacityAnimation.animatedValue * 255).toInt()
        val cachedTargetedEntity = this.cachedTargetedEntity

        val textRenderer = MinecraftClient.getInstance().textRenderer
        if (cachedTargetedEntity != null) {
            val red = ColorHelper.Argb.getArgb(descriptionAlpha, 255, 0, 0)
            val green = ColorHelper.Argb.getArgb(descriptionAlpha, 0, 255, 0)

            currentHealthAnimation.currentValue = cachedTargetedEntity.health.toDouble()
            maxHealthAnimation.currentValue = cachedTargetedEntity.maxHealth.toDouble()

            val health = cachedTargetedEntity.health
            val maxHealth = cachedTargetedEntity.maxHealth
            val percentage = currentHealthAnimation.animatedValue / maxHealthAnimation.animatedValue
            val lerpedColor = ColorHelper.Argb.lerp(percentage.toFloat(), red, green)

            val progressBarX = MathHelper.lerp(percentage.toFloat(), 190, 35)
            drawContext.fill(drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt(), drawContext.scaledWindowHeight / 2 - 80, drawContext.scaledWindowWidth - progressBarX - magicShownAnimation.animatedValue.toInt(), drawContext.scaledWindowHeight / 2 - 75, lerpedColor)

            val descriptionForegroundColor = ColorHelper.Argb.getArgb(descriptionAlpha, 255, 255, 255)
            if (descriptionAlpha > 3) {
                drawContext.drawText(textRenderer, StringBuilder().append((health * 100).toLong().toDouble() / 100).append("/").append((maxHealth * 100).toLong().toDouble() / 100).toString(), drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt(), drawContext.scaledWindowHeight / 2 - 70, descriptionForegroundColor, false)
            }
            if (descriptionAlpha > 3) {
                drawContext.drawText(textRenderer, cachedTargetedEntity.name, drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt(), drawContext.scaledWindowHeight / 2 - 90, descriptionForegroundColor, false)
            }
        }

        if (alpha <= 3) {
            return
        }

        val primaryAlpha = min(alpha.toDouble(), 255 * magicDescriptionPrimaryOpacityAnimation.animatedValue).toInt()
        val secondaryAlpha = min(alpha.toDouble(), 255 * magicDescriptionSecondaryOpacityAnimation.animatedValue).toInt()

        val primaryForegroundColor = ColorHelper.Argb.getArgb(primaryAlpha, 255, 255, 255)
        val secondaryForegroundColor = ColorHelper.Argb.getArgb(secondaryAlpha, 255, 255, 255)

        drawContext.enableScissor(drawContext.scaledWindowWidth - 200 - magicShownAnimation.animatedValue.toInt(), drawContext.scaledWindowHeight / 2 - 100, drawContext.scaledWindowWidth - 25 - magicShownAnimation.animatedValue.toInt(), drawContext.scaledWindowHeight / 2 + 100)
        val currentMagic = MatrixClient.getPlayerMagics()[selectedIndex - 1]
        val previousMagic = MatrixClient.getPlayerMagics()[previousIndex - 1]

        val primaryY = drawContext.scaledWindowHeight / 2 - 55 + descriptionYOffsetAnimation.animatedValue.toInt() // + magicSwitchAnimation.animatedValue

        if (primaryAlpha > 3) {
            drawContext.drawTextWrapped(textRenderer, currentMagic.description, drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt(), primaryY, 150, primaryForegroundColor)
        }
        if (secondaryAlpha > 3) {
            drawContext.drawTextWrapped(textRenderer, previousMagic.description, drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt(), primaryY, 150, secondaryForegroundColor)
        }
        drawContext.disableScissor()
    }

    private fun getTargetedEntity(tickDelta: Float): Entity? {
        val range = 10000.0

        val minecraftClient = MinecraftClient.getInstance()!!
        val camera = minecraftClient.cameraEntity!!

        val location = camera.getCameraPosVec(tickDelta)
        val rotation = camera.getRotationVec(tickDelta)
        val min = location.add(rotation)
        val max = location.add(rotation.multiply(range))
        val box = Box(min, max)

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

    private fun generateIndentList(
        size: Int,
    ): List<Int> {
        val result = mutableListOf<Int>()

        var current = 0
        for (i in 0 until size) {
            result.add(
                current
            )

            if (size % 2 == 0 && i == size / 2 - 1) {
                continue
            }

            if (i >= size / 2) {
                current += 5
            } else {
                current -= 5
            }
        }

        return result
    }
}