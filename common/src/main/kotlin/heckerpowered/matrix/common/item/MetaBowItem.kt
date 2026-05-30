/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.item.ModComponents.shootPerMinute
import heckerpowered.matrix.core.utility.FixedRateRepeater
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import kotlin.time.Duration.Companion.minutes

object MetaBowItem : BowItem(
    Properties()
        .durability(3840)
        .component(shootPerMinute, 3600)
) {
    private val repeater = FixedRateRepeater(1.minutes, 3600)

    init {
        ServerTickEvents.START_SERVER_TICK.register(::onServerTick)
    }

    private fun onServerTick(@Suppress("unused") server: MinecraftServer) {
        repeater.consumePendingOperations()
        repeater.update()
    }

    override fun onUseTick(level: Level, livingEntity: LivingEntity, itemStack: ItemStack, ticksRemaining: Int) {
        super.onUseTick(level, livingEntity, itemStack, ticksRemaining)
        if (livingEntity !is Player) return

        val shootPerMinute = itemStack.components.getOrDefault(shootPerMinute, 3600)
        repeat(repeater.getPendingOperationsIn(repeats = shootPerMinute).toInt()) {
            shoot(livingEntity, itemStack, 1.0F)
        }
    }

    fun shoot(user: Player, stack: ItemStack, pullProgress: Float) {
        val level = user.level()
        val projectileItem = user.getProjectile(stack)
        val list = draw(stack, projectileItem, user)
        if (level is ServerLevel && !list.isEmpty()) {
            shoot(level, user, user.usedItemHand, stack, list, pullProgress * 3.0f, 1.0f, pullProgress == 1.0f, null)
        }

        level.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.ARROW_SHOOT,
            SoundSource.PLAYERS,
            1.0f,
            1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + pullProgress * 0.5f
        )
        user.awardStat(Stats.ITEM_USED.get(this))
    }
}