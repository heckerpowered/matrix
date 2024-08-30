package heckerpowered.matrix.client

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
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.MathHelper


object MatrixHud {
    var mana = 100
    var maxMana = 100

    private var targetedEntity: Entity? = null

    private var ticks: Long = 0

    private val backgroundColor = ColorHelper.Argb.getArgb(128, 0, 0, 0)
    private val foregroundColor = ColorHelper.Argb.getArgb(255, 255, 255, 255)

    private var selectedIndex = 1
    private var usingMagicList = mutableMapOf<Int, Double>()

    private val magicQueue = mutableListOf<Pair<Int, Int>>()

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

        ClientTickEvents.START_CLIENT_TICK.register {
            if ((ticks % 5L).toInt() == 0) {
                ++mana
                mana = mana.coerceAtMost(maxMana)
            }
            ++ticks
        }
    }

    fun nextMagic() {
        ++selectedIndex
        if (selectedIndex >= MatrixClient.getPlayerMagics().size) {
            selectedIndex = 1
        }
    }

    fun previousMagic() {
        --selectedIndex
        if (selectedIndex < 1) {
            selectedIndex = MatrixClient.getPlayerMagics().size
        }
    }

    private fun useCurrentMagic() {
        if (!shouldRenderHud()) {
            return
        }

        val magic = MagicManager.getMagic(MinecraftClient.getInstance().player!!, selectedIndex) ?: return
        val targetedEntity = this.targetedEntity ?: return

        if (mana < magic.cost) {
            return
        }
        mana -= magic.cost

        usingMagicList[selectedIndex] = 0.0
        magicQueue.add(Pair(selectedIndex, targetedEntity.id))
    }

    private fun onHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        if (!shouldRenderHud()) {
            for (magic in magicQueue) {
                ClientPlayNetworking.send(UseMagicPayload(magic.first, magic.second))
            }
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

    private fun shouldRenderHud() = MinecraftClient.getInstance().options.playerListKey.isPressed

    private fun renderManaBar(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val manaBarColor = ColorHelper.Argb.getArgb(128, 0, 128, 255)

        drawContext.fill(
            50,
            10,
            MathHelper.lerp(mana.toDouble() / maxMana.toDouble(), 50.0, drawContext.scaledWindowWidth - 50.0)
                .toInt(),
            25,
            0,
            manaBarColor
        )

        val textRenderer = MinecraftClient.getInstance().textRenderer
        drawContext.drawText(
            textRenderer,
            Text.literal("法力值"),
            55,
            13,
            foregroundColor,
            true
        )

        // val cost = 10
        // val maxMana = 100

        val barWidth = drawContext.scaledWindowWidth - 100

        // drawContext.fill(
        //     drawContext.scaledWindowWidth - 50 - (barWidth.toDouble() * (cost.toDouble() / maxMana.toDouble())).toInt(),
        //     10,
        //     drawContext.scaledWindowWidth - 50,
        //     25,
        //     0,
        //     ColorHelper.Argb.getArgb(255, 255, 0, 0)
        // )
    }

    private fun renderLeftPart(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        /**
        drawContext.fill(
        25,
        drawContext.scaledWindowHeight - 25,
        100,
        25,
        0,
        backgroundColor
        )
         */

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

            val startX = (xIndent + 30 + deltaTime * 5).toInt()
            val endX = (startX + 10).coerceAtMost(maxX)

            if (startX > maxX) {
                usingMagicList.remove(index)
                return
            }

            drawContext.fill(
                startX,
                startY,
                endX,
                endY,
                0,
                ColorHelper.Argb.getArgb(128, 0, 0, 128)
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