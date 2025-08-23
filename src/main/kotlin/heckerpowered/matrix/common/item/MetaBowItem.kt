/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.item.MatrixComponents.SHOOT_PER_MINUTE
import heckerpowered.matrix.core.utility.FixedRateRepeater
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BowItem
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.world.World
import kotlin.time.Duration.Companion.minutes

object MetaBowItem : BowItem(
    Settings()
        .maxDamage(3840)
        .component(SHOOT_PER_MINUTE, 3600)
) {
    private val repeater = FixedRateRepeater(1.minutes, 3600)

    init {
        ServerTickEvents.START_SERVER_TICK.register(::onServerTick)
    }

    private fun onServerTick(server: MinecraftServer) {
        repeater.consumePendingOperations()
        repeater.update()
    }

    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        super.usageTick(world, user, stack, remainingUseTicks)
        if (user !is PlayerEntity) {
            return
        }

        val shootPerMinute = stack.components.getOrDefault(SHOOT_PER_MINUTE, 3600)
        repeat(repeater.getPendingOperationsIn(repeats = shootPerMinute).toInt()) {
            shoot(user, stack, 1.0F)
        }
    }

    fun shoot(user: PlayerEntity, stack: ItemStack, pullProgress: Float) {
        val world = user.world
        val projectileItem = user.getProjectileType(stack)
        val list = load(stack, projectileItem, user)
        if (world is ServerWorld && !list.isEmpty()) {
            shootAll(world, user, user.activeHand, stack, list, pullProgress * 3.0f, 1.0f, pullProgress == 1.0f, null)
        }

        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.ENTITY_ARROW_SHOOT,
            SoundCategory.PLAYERS,
            1.0f,
            1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pullProgress * 0.5f
        )
        user.incrementStat(Stats.USED.getOrCreateStat(this))
    }
}