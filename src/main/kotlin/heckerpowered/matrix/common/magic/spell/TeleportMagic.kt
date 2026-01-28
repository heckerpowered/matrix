/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.core.squaredDistanceTo
import heckerpowered.matrix.core.toBox
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity

object TeleportMagic : Magic(
    MagicDefinition(
        Matrix.identifier("teleport"),
        15.mana,
        5.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.entityOrNull() ?: return
        val target = invocation.target

        val velocity = caster.velocity
        caster.teleport(target.x, target.y, target.z, true)
        caster.velocity = velocity
        caster.velocityDirty = true
        caster.velocityModified = true
        if (caster is ServerPlayerEntity) {
            caster.networkHandler.sendPacket(EntityVelocityUpdateS2CPacket(caster))
        }

        target.world.getOtherEntities(caster, target.pos.toBox().expand(3.0))
            .filter { it squaredDistanceTo caster <= 6 * 6 }
            .forEach {
                it.timeUntilRegen = 0
                caster.lastAttackedTicks = Int.MAX_VALUE
                if (caster is PlayerEntity) {
                    caster.attack(it)
                    caster.addCritParticles(it)
                    caster.addEnchantedHitParticles(it)
                }
                caster.swingHand(caster.activeHand, true)
            }
    }
}