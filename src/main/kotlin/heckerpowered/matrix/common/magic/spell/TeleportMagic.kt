/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Magic
import heckerpowered.matrix.common.magic.MagicDefinition
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.magic.channel.ChannelQueue
import heckerpowered.matrix.common.magic.core.ExecutionPayload
import heckerpowered.matrix.core.attack
import heckerpowered.matrix.core.squaredDistanceTo
import heckerpowered.matrix.core.toBox
import net.minecraft.entity.LivingEntity
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity

object TeleportMagic : Magic(
    MagicDefinition(
        Matrix.identifier("teleport"),
        15.mana,
        5.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: ExecutionPayload) {
        super.cast(player, target, sequence, data)
        if (player == null) {
            return
        }

        val velocity = player.velocity
        player.teleport(target.x, target.y, target.z, true)
        player.networkHandler.sendPacket(EntityVelocityUpdateS2CPacket(player))
        player.velocity = velocity
        player.velocityDirty = true
        player.velocityModified = true

        target.world.getOtherEntities(player, target.pos.toBox().expand(3.0))
            .filter { it squaredDistanceTo player <= 6 * 6 }
            .forEach {
                it.timeUntilRegen = 0
                player.lastAttackedTicks = Int.MAX_VALUE
                player attack it
                player.addCritParticles(it)
                player.addEnchantedHitParticles(it)
                player.swingHand(player.activeHand, true)
            }
    }
}