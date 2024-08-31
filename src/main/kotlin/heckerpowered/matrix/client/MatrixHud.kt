package heckerpowered.matrix.client

import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.MatrixUIRenderer
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.render.Rectangle
import heckerpowered.matrix.client.ui.element.ManaBar
import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.MagicManager
import heckerpowered.matrix.common.network.UseMagicPayload
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.MathHelper

object MatrixHud {
    var mana
        get() = manaBar.currentMana
        set(value) {
            manaBar.currentMana = value
        }
    var maxMana
        get() = manaBar.maxMana
        set(value) {
            manaBar.maxMana = value
        }
    private var manaUsage
        get() = manaBar.manaUsage
        set(value) {
            manaBar.manaUsage = value
        }

    private var targetedEntity: Entity? = null

    private var ticks: Long = 0

    private val backgroundColor = ColorHelper.Argb.getArgb(128, 0, 0, 0)
    private val foregroundColor = ColorHelper.Argb.getArgb(255, 255, 255, 255)

    private var selectedIndex = 1
    private var usingMagicList = mutableMapOf<Int, Double>()

    private val magicQueue = mutableListOf<Pair<Int, Int>>()

    private val manaBar = ManaBar()

    fun onInitialize() {
        HudRenderCallback.EVENT.register(this::onHudRender)

        ClientTickEvents.END_CLIENT_TICK.register {
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
        }
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
        manaBar.manaCost = MatrixClient.getPlayerMagics()[selectedIndex - 1].cost.toDouble()
    }

    private fun useCurrentMagic() {
        if (!shouldRenderHud()) {
            return
        }

        val magic = MagicManager.getMagic(MinecraftClient.getInstance().player!!, selectedIndex) ?: return
        val targetedEntity = this.targetedEntity ?: return

        if (manaUsage + magic.cost > mana) {
            return
        }
        manaUsage += magic.cost

        usingMagicList[selectedIndex] = 0.0
        magicQueue.add(Pair(selectedIndex, targetedEntity.id))

        val minecraft = MinecraftClient.getInstance()
        val player = minecraft.player!!
        minecraft.world!!.playSound(
            player,
            player.x, player.y, player.z,
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.PLAYERS
        )
    }

    private fun onHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        if (!shouldRenderHud()) {
            useMagics()
            renderManaBarAutoHide(drawContext, tickCounter)
            return
        }

        // Render right part
        renderManaBar(drawContext, tickCounter)
        renderLeftPart(drawContext, tickCounter)
        renderRightPart(drawContext, tickCounter)

        for (magic in usingMagicList) {
            magic.setValue(magic.value + tickCounter.getTickDelta(true))
        }
    }

    private fun useMagics() {
        if (manaBar.manaCost != .0) {
            manaBar.manaCost = .0
        }
        if (magicQueue.isEmpty()) {
            return
        }

        for (magic in magicQueue) {
            ClientPlayNetworking.send(UseMagicPayload(magic.first, magic.second))
        }
        magicQueue.clear()
        mana -= manaUsage
        manaUsage = .0
        ticks = 0
    }

    private fun shouldRenderHud() = MinecraftClient.getInstance().options.playerListKey.isPressed

    private fun renderManaBarAutoHide(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val renderer = MatrixUIRenderer(drawContext.vertexConsumers)
        manaBar.renderManaBarAutoHide(renderer)
    }

    private fun renderManaBar(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val magicCost = MatrixClient.getPlayerMagics()[selectedIndex - 1].cost.toDouble()
        if (manaBar.manaCost != magicCost) {
            manaBar.manaCost = magicCost
        }

        val renderer = MatrixUIRenderer(drawContext.vertexConsumers)
        manaBar.render(renderer)
    }

    private fun renderLeftPart(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val magics = MatrixClient.getPlayerMagics()
        val indentList = generateIndentList(magics.size)
        repeat(magics.size) {
            renderMagic(drawContext, it + 1, magics[it], indentList)
        }
    }

    private fun renderMagic(drawContext: DrawContext, index: Int, magic: Magic, indentList: List<Int>) {
        // Render a widget start from y30, ends at y50

        val xIndent = indentList[index - 1]

        val color = if (selectedIndex == index) {
            ColorHelper.Argb.getArgb(128, 0, 128, 0)
        } else if (manaUsage + magic.cost > mana) {
            ColorHelper.Argb.getArgb(128, 128, 0, 0)
        } else {
            backgroundColor
        }

        val startY = index * 30
        val endY = startY + 20

        drawContext.fill(
            xIndent + 30,
            startY,
            xIndent + 95,
            endY,
            0,
            color
        )

        val textRenderer = MinecraftClient.getInstance().textRenderer
        drawContext.drawText(
            textRenderer,
            magic.text,
            xIndent + 35,
            startY + 5,
            foregroundColor,
            false
        )

        drawContext.drawText(
            textRenderer,
            Text.literal(magic.cost.toString()),
            xIndent + 80,
            startY + 5,
            foregroundColor,
            false
        )

        if (usingMagicList.containsKey(index)) {
            val deltaTime = usingMagicList[index]!!

            val maxX = xIndent + 95

            val startX = (xIndent + 30 + deltaTime * 5)
            val endX = (startX + 10).coerceAtMost(maxX.toDouble())

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
                Color(0, 0, 128, 128)
            )
        }
    }

    private fun renderRightPart(drawContext: DrawContext, tickCounter: RenderTickCounter) {
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

        var y = 55
        val currentMagic = MatrixClient.getPlayerMagics()[selectedIndex - 1]
        for (description in currentMagic.getDescription()) {
            drawContext.drawText(
                textRenderer,
                description,
                drawContext.scaledWindowWidth - 90,
                y,
                foregroundColor,
                false
            )
            y += 10
        }
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
}