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
import heckerpowered.matrix.core.utility.getOtherEntities
import heckerpowered.matrix.core.utility.withinDistance
import heckerpowered.matrix.mixin.LivingEntityAccessor
import net.minecraft.world.entity.player.Player

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

        caster.snapTo(target.x, target.y, target.z)

        target.getOtherEntities(6.0)
            .withinDistance(target, 6.0)
            .filter { it != caster }
            .forEach {
                it.invulnerableTime = 0
                (caster as LivingEntityAccessor).`matrix$setAttackStrengthTicker`(Int.MAX_VALUE)
                if (caster is Player) {
                    caster.attack(it)
                    caster.crit(it)
                    caster.magicCrit(it)
                }
                caster.swing(caster.usedItemHand, true)
            }
    }
}