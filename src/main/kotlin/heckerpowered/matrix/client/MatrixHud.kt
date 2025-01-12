package heckerpowered.matrix.client

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.MatrixUIRenderer
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.render.Rectangle
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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.PostEffectProcessor
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
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
import java.time.Duration

object MatrixHud {
    var mana
        get() = manaBar.mana.currentValue
        set(value) {
            manaBar.mana.currentValue = value.coerceIn(0.0..maxMana)
        }

    var maxMana
        get() = manaBar.maxMana.currentValue
        set(value) {
            manaBar.maxMana.currentValue = value
        }

    var manaUsage
        get() = manaBar.manaUsage
        set(value) {
            manaBar.manaUsage = value
        }

    private var targetedEntity: Entity? = null

    private var ticks = 0L
    private var lastNanos = 0L

    private val backgroundColor = ColorHelper.Argb.getArgb(128, 0, 0, 0)
    private val foregroundColor = ColorHelper.Argb.getArgb(255, 255, 255, 255)

    private var selectedIndex = 1
    private var usingMagicList = mutableMapOf<Int, Double>()

    private val magicQueue = mutableMapOf<Int, MutableList<Int>>()

    private val manaBar = ManaBar()

    private val easingFunction = ElasticEase()

    private val manaOverclockAnimation = AnimationClock(Duration.ofMillis(300), 1.0, 1.0)
    private val magicOverclockAnimation = AnimationClock(Duration.ofMillis(300), 1.0, 1.0)

    private val magicShownAnimationClock = AnimationClock(Duration.ofMillis(300), -50.0, .0)
    private val magicShownOpacityAnimationClock = AnimationClock(Duration.ofMillis(300), .0, 128.0)
    private val magicShownAnimation = DoubleAnimation(magicShownAnimationClock, easingFunction)
    private val magicShownOpacityAnimation = DoubleAnimation(magicShownOpacityAnimationClock, easingFunction)
    private val timeSlowAnimationClock = AnimationClock(Duration.ofMillis(300), 1.0, 0.01)
    private val timeSlowAnimation = DoubleAnimation(timeSlowAnimationClock, easingFunction)

    private val manaOverclock = DoubleAnimation(manaOverclockAnimation, easingFunction)
    private val magicOverclock = DoubleAnimation(magicOverclockAnimation, easingFunction)

    private var previousVisibility = false

    private var colorFilter: PostEffectProcessor? = null

    init {
        easingFunction.easingMode = EasingMode.OUT
        easingFunction.oscillations = 0

        magicOverclock.currentValue = 1.0
        manaOverclock.currentValue = 1.0
    }

    private class ColorAnimation {
        val redClock = AnimationClock(Duration.ofMillis(300), .0, 1.0)
        val red = DoubleAnimation(redClock, easingFunction)

        val greenClock = AnimationClock(Duration.ofMillis(300), .0, 1.0)
        val green = DoubleAnimation(greenClock, easingFunction)

        val blueClock = AnimationClock(Duration.ofMillis(300), .0, 1.0)
        val blue = DoubleAnimation(blueClock, easingFunction)

        fun setColor(red: Double, green: Double, blue: Double) {
            this.red.currentValue = red
            this.green.currentValue = green
            this.blue.currentValue = blue
        }

        fun setColorWithoutAnimation(red: Double, green: Double, blue: Double) {
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
            val minecraftClient = MinecraftClient.getInstance()
            val difference = if (minecraftClient.player!!.isSneaking) -0.5 else 0.5
            val newOverclock = (magicOverclock.currentValue + difference).coerceIn(1.0..10.0)
            magicOverclock.currentValue = newOverclock
            ClientPlayNetworking.send(OverclockPayload(manaOverclock.currentValue, magicOverclock.currentValue))
        }

        while (MatrixKeyBindings.overclockMana.wasPressed()) {
            val minecraftClient = MinecraftClient.getInstance()
            val difference = if (minecraftClient.player!!.isSneaking) -0.5 else 0.5
            val newOverclock = (manaOverclock.currentValue + difference).coerceIn(1.0..10.0)
            manaOverclock.currentValue = newOverclock
            ClientPlayNetworking.send(OverclockPayload(manaOverclock.currentValue, magicOverclock.currentValue))
        }
    }

    fun onInitialize() {
        HudRenderCallback.EVENT.register(this::onHudRender)
        SystemCrashBar.onInitialize()
    }

    @JvmStatic
    fun overclockMana() {
        val newOverclock = (manaOverclock.currentValue + 0.5).coerceIn(1.0..10.0)
        manaOverclock.currentValue = newOverclock
        ClientPlayNetworking.send(OverclockPayload(manaOverclock.currentValue, magicOverclock.currentValue))
    }

    @JvmStatic
    fun underclockMana() {
        val newOverclock = (manaOverclock.currentValue - 0.5).coerceIn(1.0..10.0)
        manaOverclock.currentValue = newOverclock
        ClientPlayNetworking.send(OverclockPayload(manaOverclock.currentValue, magicOverclock.currentValue))
    }

    @JvmStatic
    fun overclockMagic() {
        val newOverclock = (magicOverclock.currentValue + 0.5).coerceIn(1.0..10.0)
        magicOverclock.currentValue = newOverclock
        ClientPlayNetworking.send(OverclockPayload(manaOverclock.currentValue, magicOverclock.currentValue))
    }

    @JvmStatic
    fun underclockMagic() {
        val newOverclock = (magicOverclock.currentValue - 0.5).coerceIn(1.0..10.0)
        magicOverclock.currentValue = newOverclock
        ClientPlayNetworking.send(OverclockPayload(manaOverclock.currentValue, magicOverclock.currentValue))
    }

    @JvmStatic
    fun nextMagic() {
        ++selectedIndex
        if (selectedIndex > MatrixClient.getPlayerMagics().size) {
            selectedIndex = 1
        }
        onSelectionChanged()
    }

    @JvmStatic
    fun previousMagic() {
        --selectedIndex
        if (selectedIndex < 1) {
            selectedIndex = MatrixClient.getPlayerMagics().size
        }
        onSelectionChanged()
    }

    private fun onSelectionChanged() {
        manaBar.manaCost = MatrixClient.getPlayerMagics()[selectedIndex - 1].getCost().toDouble()
    }

    private fun useCurrentMagic() {
        if (!shouldRenderHud()) {
            return
        }

        val magic = MatrixClient.getPlayerMagics()[selectedIndex - 1]
        val targetedEntity = this.targetedEntity ?: return
        if (targetedEntity !is LivingEntity) {
            return
        }

        val minecraft = MinecraftClient.getInstance()
        val player = MinecraftClient.getInstance().player!!
        if (magic.availableStatus(
                player,
                targetedEntity as LivingEntity?,
                ChannelSequence.getChannelSequence(player, targetedEntity as LivingEntity?)
            ) != AVAILABLE
        ) {
            return
        }
        // manaUsage += magic.getCost()

        usingMagicList[selectedIndex] = 0.0
        magicQueue.computeIfAbsent(targetedEntity.id) { mutableListOf() }.add(magic.name.hashCode())

        minecraft.world!!.playSound(
            player,
            player.x, player.y, player.z,
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.PLAYERS,
            1.0f,
            1f
        )

        magicQueue.forEach {
            val entityId = it.key
            val magics = it.value.toIntArray()
            ClientPlayNetworking.send(UseMagicPayload(magics, entityId))
        }

        magicQueue.clear()
        ChannelSequence.channelMagic(magic, player, targetedEntity)
        ChannelSequence.channelMagicClient(magic, targetedEntity)
        // useMagics()
    }

    private fun checkVisibilityChanges() {
        val shouldRenderHud = shouldRenderHud()
        if (shouldRenderHud != previousVisibility) {
            onHudVisibilityChanged(shouldRenderHud)
        }
        previousVisibility = shouldRenderHud
    }

    private fun wrapDancer() {
        val minecraft = MinecraftClient.getInstance()
        if (minecraft.server != null && minecraft.server!!.currentPlayerCount > 1) {
            return
        }

        if (timeSlowAnimation.animatedValue != 1.0 && timeSlowAnimation.animatedValue != 0.01) {
            Wrap.setClientTimeScale(timeSlowAnimation.animatedValue)
        }
    }

    private fun checkMagicAvailableStatus() {
        val magics = MatrixClient.getPlayerMagics()
        fillColorAnimationList(magics)

        magics.forEachIndexed { index, magic ->
            if (index == selectedIndex - 1) {
                magicColorAnimations[index].setColorWithoutAnimation(.0, 128.0, .0)
                return@forEachIndexed
            }

            val status = getMagicAvailableStatus(magic)
            when (status) {
                AVAILABLE_MANA_NOT_ENOUGH -> magicColorAnimations[index].setColor(128.0, 0.0, 0.0)
                TARGET_IMMUNE -> magicColorAnimations[index].setColor(128.0, 0.0, 0.0)
                UNAVAILABLE -> magicColorAnimations[index].setColor(128.0, 0.0, 0.0)
                CHANNEL_QUEUE_FULL -> magicColorAnimations[index].setColor(128.0, 0.0, 0.0)
                CHANNEL_QUEUE_LOCKED -> magicColorAnimations[index].setColor(128.0, 0.0, 0.0)
                TARGET_MISSING -> magicColorAnimations[index].setColor(.0, .0, .0)
                AVAILABLE -> magicColorAnimations[index].setColor(.0, .0, .0)
            }
        }
    }

    private fun fillColorAnimationList(magics: List<Magic>) {
        if (magicColorAnimations.size < magics.size) {
            for (i in magicColorAnimations.size..magics.size) {
                magicColorAnimations.add(ColorAnimation())
            }
        }
    }

    private fun fillExtraWidthAnimationList(magics: List<Magic>) {
        if (magicExtraWidthAnimations.size < magics.size) {
            for (i in magicExtraWidthAnimations.size..magics.size) {
                val animationClock = AnimationClock(Duration.ofMillis(300), 0.0, 1.0)
                val animation = DoubleAnimation(animationClock, easingFunction)
                magicExtraWidthAnimations.add(
                    PackedAnimation(
                        animationClock,
                        animation
                    )
                )
            }
        }
    }

    private fun getMagicAvailableStatus(magic: Magic): MagicAvailableStatus {
        val player = MinecraftClient.getInstance().player!!
        val targetedEntity = this.targetedEntity as? LivingEntity?
        val channelSequence = ChannelSequence.getChannelSequence(player, targetedEntity)
        return magic.availableStatus(player, targetedEntity, channelSequence)
    }

    private fun renderMagicAvailableStatus(renderer: MatrixUIRenderer) {
        val magic = MatrixClient.getPlayerMagics()[selectedIndex - 1]
        val status = getMagicAvailableStatus(magic)
        if (status == AVAILABLE || status == TARGET_MISSING || !shouldRenderHud()) {
            AvailableStatusTooltip.hide()
        } else {
            AvailableStatusTooltip.show()
        }

        AvailableStatusTooltip.render(renderer, status)
    }

    private fun onHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val renderer = MatrixUIRenderer(drawContext.vertexConsumers)
        renderMagicAvailableStatus(renderer)

        processKeyInput()
        checkVisibilityChanges()
        wrapDancer()
        checkMagicAvailableStatus()

        ticks++

        renderLeftPart(drawContext, tickCounter)
        renderRightPart(drawContext, tickCounter)

        val delta = Util.getMeasuringTimeNano() - lastNanos
        for (magic in usingMagicList) {
            magic.setValue(magic.value + delta / 2000000)
        }

        if (!shouldRenderHud()) {
            useMagics()
            renderManaBarAutoHide(drawContext, tickCounter)
            return
        }

        selectTargetEntity(tickCounter)
        renderManaBar(drawContext, tickCounter)
        renderOverclock(drawContext, tickCounter)

        lastNanos = Util.getMeasuringTimeNano()
    }

    private fun useMagics() {
        if (magicQueue.isEmpty()) {
            return
        }

        for (magic in magicQueue) {
            val entityId = magic.key
            val magics = magic.value.toIntArray()
            ClientPlayNetworking.send(UseMagicPayload(magics, entityId))
        }

        magicQueue.clear()
        mana -= manaUsage
        manaUsage = .0
        ticks = 0
    }

    private fun shouldRenderHud() = MinecraftClient.getInstance().options.playerListKey.isPressed

    private fun onHudVisibilityChanged(visibility: Boolean) {
        manaBar.onHudVisibilityChanged(visibility)
        if (visibility) {
            // showBlueFilter()
            Wrap.setTimeScale(0.01)

            magicShownAnimationClock.let {
                it.from = magicShownAnimation.animatedValue
                it.to = .0
            }

            magicShownOpacityAnimationClock.let {
                it.from = magicShownOpacityAnimation.animatedValue
                it.to = 128.0
            }

            timeSlowAnimationClock.let {
                it.from = timeSlowAnimation.animatedValue
                it.to = 0.01
            }

            magicShownAnimationClock.start()
            magicShownOpacityAnimationClock.start()
            timeSlowAnimationClock.start()
        } else {
            Wrap.setTimeScale(1.0)

            magicShownAnimationClock.let {
                it.from = magicShownAnimation.animatedValue
                it.to = -50.0
            }

            magicShownOpacityAnimationClock.let {
                it.from = magicShownOpacityAnimation.animatedValue
                it.to = .0
            }

            timeSlowAnimationClock.let {
                it.from = timeSlowAnimation.animatedValue
                it.to = 1.0
            }

            magicShownAnimationClock.start()
            magicShownOpacityAnimationClock.start()
            timeSlowAnimationClock.start()
        }
    }

    private fun renderManaBarAutoHide(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val renderer = MatrixUIRenderer(drawContext.vertexConsumers)
        manaBar.renderManaBarAutoHide(renderer)
    }

    private fun renderManaBar(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val magicCost = MatrixClient.getPlayerMagics()[selectedIndex - 1].getCost().toDouble()
        if (manaBar.manaCost != magicCost) {
            manaBar.manaCost = magicCost
        }

        val renderer = MatrixUIRenderer(drawContext.vertexConsumers)
        manaBar.render(renderer)
    }

    private fun renderLeftPart(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val magics = MatrixClient.getPlayerMagics()
        val indentList = generateIndentList(magics.size)
        fillExtraWidthAnimationList(magics)
        repeat(magics.size) {
            renderMagic(drawContext, it + 1, magics[it], indentList)
        }
    }

    private fun restrictedSizedString(string: String, width: Double): String {
        val textRenderer = MinecraftClient.getInstance().textRenderer
        var length = 0
        var index = 0
        for (character in string) {
            length += textRenderer.getWidth(character.toString())
            if (length > width) {
                return string.substring(0, index - 1)
            }
            ++index
        }

        return string
    }


    private fun renderMagic(drawContext: DrawContext, index: Int, magic: Magic, indentList: List<Int>) {
        // Render a widget start from y30, ends at y50

        val xIndent = indentList[index - 1]

        val animatedColor = magicColorAnimations.getOrNull(index - 1)
        val color = ColorHelper.Argb.getArgb(
            magicShownOpacityAnimation.animatedValue.toInt(),
            animatedColor?.red?.animatedValue?.toInt() ?: 0,
            animatedColor?.green?.animatedValue?.toInt() ?: 0,
            animatedColor?.blue?.animatedValue?.toInt() ?: 0
        )

        val height = 20.0
        val margin = 5.0

        val startY =
            (index * (height + margin) + drawContext.scaledWindowHeight / 2 - (indentList.size + 1) * (height + margin) / 2).toInt()
        val endY = (startY + height).toInt()

        val status = getMagicAvailableStatus(magic).let {
            if (it == TARGET_MISSING) {
                AVAILABLE
            } else {
                it
            }
        }
        val costString = magic.getCost().toString()
        val statusString = status.description

        val textRenderer = MinecraftClient.getInstance().textRenderer
        val magicNameWidth = textRenderer.getWidth(magic.name)
        val costStringWidth = textRenderer.getWidth(costString)
        val statusStringWidth = textRenderer.getWidth(statusString)
        val extraWidth = magicNameWidth + costStringWidth + statusStringWidth + 25 /* padding */
        val extraWidthAnimation = magicExtraWidthAnimations[index - 1].animation
        extraWidthAnimation.currentValue = extraWidth.toDouble()

        drawContext.fill(
            xIndent + 50 + magicShownAnimation.animatedValue.toInt(),
            startY,
            xIndent + 55 + extraWidthAnimation.animatedValue.toInt() + magicShownAnimation.animatedValue.toInt(),
            endY,
            0,
            color
        )

        val alpha = magicShownOpacityAnimation.animatedValue.toInt() * 2
        val foregroundColor = ColorHelper.Argb.getArgb(alpha, 255, 255, 255)
        if (alpha > 5) {
            drawContext.drawText(
                textRenderer,
                magic.name,
                xIndent + 55 + magicShownAnimation.animatedValue.toInt(),
                startY + 5,
                foregroundColor,
                false
            )

            drawContext.drawText(
                textRenderer,
                Text.literal(costString),
                xIndent + 65 + magicNameWidth + magicShownAnimation.animatedValue.toInt(),
                startY + 5,
                foregroundColor,
                false
            )

            val statusStringMaxWidth = extraWidthAnimation.animatedValue - magicNameWidth - costStringWidth - 20
            val restrictedStatusString = restrictedSizedString(statusString.string, statusStringMaxWidth)
            drawContext.drawText(
                textRenderer,
                Text.of(restrictedStatusString),
                xIndent + 75 + magicNameWidth + costStringWidth + magicShownAnimation.animatedValue.toInt(),
                startY + 5,
                foregroundColor,
                false
            )
        }

        if (usingMagicList.containsKey(index)) {
            val deltaTime = usingMagicList[index]!!

            val maxX = xIndent + 150

            val startX = (xIndent + 50 + deltaTime) + magicShownAnimation.animatedValue.toInt()
            val endX = (startX + 10).coerceAtMost(maxX.toDouble()) + magicShownAnimation.animatedValue.toInt()

            if (startX > maxX) {
                usingMagicList.remove(index)
                return
            }

            val renderer = MatrixUIRenderer(drawContext.vertexConsumers)
            renderer.renderRectangle(
                Rectangle(
                    Point(startX, startY.toDouble()),
                    Point(endX, endY.toDouble())
                ),
                Color(0, 0, 128, magicShownOpacityAnimation.animatedValue.toInt())
            )
        }
    }

    private fun renderOverclock(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val renderer = MatrixUIRenderer(drawContext.vertexConsumers)
        val manaOverclockMinPoint = Point(
            renderer.scaledWindowWidth - 5.0,
            renderer.scaledWindowHeight.toDouble() - 25.0
        )
        val manaOverclockMaxPoint = Point(
            renderer.scaledWindowWidth - 10.0,
            MathHelper.lerp(manaOverclock.animatedValue / 10, renderer.scaledWindowHeight.toDouble() - 25.0, 25.0)
        )

        val magicOverclockMinPoint = Point(
            renderer.scaledWindowWidth - 15.0,
            renderer.scaledWindowHeight.toDouble() - 25.0
        )

        val magicOverclockMaxPoint = Point(
            renderer.scaledWindowWidth - 20.0,
            MathHelper.lerp(magicOverclock.animatedValue / 10, renderer.scaledWindowHeight.toDouble() - 25.0, 25.0)
        )

        val magicOverclockColor = Color(
            MathHelper.lerp(magicOverclock.animatedValue / 10.0, 0.0, 255.0).toInt(),
            MathHelper.lerp(magicOverclock.animatedValue / 10.0, 255.0, 0.0).toInt(),
            0, 128
        )

        val manaOverclockColor = Color(
            MathHelper.lerp(manaOverclock.animatedValue / 10.0, 0.0, 255.0).toInt(),
            MathHelper.lerp(manaOverclock.animatedValue / 10.0, 255.0, 0.0).toInt(),
            0, 128
        )

        renderer.renderRectangle(Rectangle(manaOverclockMinPoint, manaOverclockMaxPoint), manaOverclockColor)
        renderer.renderRectangle(Rectangle(magicOverclockMinPoint, magicOverclockMaxPoint), magicOverclockColor)
    }

    private fun selectTargetEntity(tickCounter: RenderTickCounter) {
        var targetedEntity = getTargetedEntity(tickCounter.getTickDelta(true))
        if (targetedEntity is EnderDragonPart) {
            targetedEntity = targetedEntity.owner
        }
        this.targetedEntity = targetedEntity
    }

    private fun renderRightPart(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val backgroundColor = ColorHelper.Argb.getArgb(magicShownOpacityAnimation.animatedValue.toInt(), 0, 0, 0)
        val alpha = (magicShownOpacityAnimation.animatedValue.toInt() * 2).coerceIn(0..255)
        val foregroundColor = ColorHelper.Argb.getArgb(alpha, 255, 255, 255)

        drawContext.fill(
            drawContext.scaledWindowWidth - 25 - magicShownAnimation.animatedValue.toInt(),
            drawContext.scaledWindowHeight / 2 - 100,
            drawContext.scaledWindowWidth - 200 - magicShownAnimation.animatedValue.toInt(),
            drawContext.scaledWindowHeight / 2 + 100,
            0,
            backgroundColor
        )

        val targetedEntity = this.targetedEntity
        if (targetedEntity != null && alpha > 5) {
            val textRenderer = MinecraftClient.getInstance().textRenderer
            drawContext.drawText(
                textRenderer,
                targetedEntity.name,
                drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt(),
                drawContext.scaledWindowHeight / 2 - 90,
                foregroundColor,
                false
            )
        }

        val textRenderer = MinecraftClient.getInstance().textRenderer
        if (targetedEntity is LivingEntity) {
            val red = ColorHelper.Argb.getArgb(alpha, 255, 0, 0)
            val green = ColorHelper.Argb.getArgb(alpha, 0, 255, 0)

            val health = targetedEntity.health
            val maxHealth = targetedEntity.maxHealth
            val percentage = health / maxHealth
            val lerpedColor = ColorHelper.Argb.lerp(percentage, red, green)

            val progressBarX = MathHelper.lerp(percentage, 190, 35)
            drawContext.fill(
                drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt(),
                drawContext.scaledWindowHeight / 2 - 80,
                drawContext.scaledWindowWidth - progressBarX - magicShownAnimation.animatedValue.toInt(),
                drawContext.scaledWindowHeight / 2 - 75,
                lerpedColor
            )

            if (alpha > 5) {
                drawContext.drawText(
                    textRenderer,
                    StringBuilder().append((health * 100).toLong().toDouble() / 100).append("/")
                        .append((maxHealth * 100).toLong().toDouble() / 100).toString(),
                    drawContext.scaledWindowWidth - 150 - magicShownAnimation.animatedValue.toInt(),
                    drawContext.scaledWindowHeight / 2 - 90,
                    foregroundColor,
                    false
                )
            }
        }

        if (alpha <= 5) {
            return
        }

        val currentMagic = MatrixClient.getPlayerMagics()[selectedIndex - 1]
        val y = drawContext.scaledWindowHeight / 2 - 65
        drawContext.drawTextWrapped(
            textRenderer,
            currentMagic.description,
            drawContext.scaledWindowWidth - 190 - magicShownAnimation.animatedValue.toInt(),
            y,
            150,
            foregroundColor
        )
    }

    @Deprecated("Legacy code, use renderRightPart instead")
    private fun renderRightPartLegacy(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val targetedEntity = getTargetedEntity(tickCounter.getTickDelta(true))
        this.targetedEntity = targetedEntity
        if (targetedEntity == null) {
            return
        }

        drawContext.fill(
            drawContext.scaledWindowWidth - 25,
            drawContext.scaledWindowHeight - 25,
            drawContext.scaledWindowWidth - 100,
            30,
            0,
            backgroundColor
        )

        val textRenderer = MinecraftClient.getInstance().textRenderer
        drawContext.drawText(
            textRenderer,
            targetedEntity.name,
            drawContext.scaledWindowWidth - 90,
            35,
            foregroundColor,
            false
        )

        if (targetedEntity is LivingEntity) {
            val red = ColorHelper.Argb.getArgb(255, 255, 0, 0)
            val green = ColorHelper.Argb.getArgb(255, 0, 255, 0)

            val health = targetedEntity.health
            val maxHealth = targetedEntity.maxHealth
            val percentage = health / maxHealth
            val lerpedColor = ColorHelper.Argb.lerp(percentage, red, green)

            val progressBarX = MathHelper.lerp(percentage, 90, 35)
            drawContext.fill(
                drawContext.scaledWindowWidth - 90,
                45,
                drawContext.scaledWindowWidth - progressBarX,
                50,
                lerpedColor
            )
        }

        val currentMagic = MatrixClient.getPlayerMagics()[selectedIndex - 1]

        drawContext.drawTextWrapped(
            textRenderer,
            currentMagic.description,
            drawContext.scaledWindowWidth - 90,
            55,
            75,
            foregroundColor
        )
    }

    private fun getTargetedEntity(tickDelta: Float): Entity? {
        val range = 10240.0

        val minecraftClient = MinecraftClient.getInstance()!!
        val camera = minecraftClient.cameraEntity!!

        val location = camera.getCameraPosVec(tickDelta)
        val rotation = camera.getRotationVec(tickDelta)
        val min = location.add(rotation)
        val max = location.add(rotation.multiply(range))
        val box = Box(min, max)

        val result = ProjectileUtil.raycast(
            camera,
            min,
            max,
            box,
            { entity ->
                !entity.isSpectator && entity.canHit()
            },
            range
        )

        return result?.entity
    }

    private fun generateIndentList(size: Int): List<Int> {
        val result = mutableListOf<Int>()

        var current = 0
        for (i in 0 until size) {
            result.add(current)

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

    private fun loadColorFilter() {
        if (colorFilter != null) {
            return
        }
        try {
            val minecraftClient = MinecraftClient.getInstance()
            colorFilter = PostEffectProcessor(
                minecraftClient.textureManager,
                minecraftClient.resourceManager,
                minecraftClient.framebuffer,
                Matrix.identifier("shaders/post/color_filter.json")
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun showBlueFilter() {
        loadColorFilter()

    }
}