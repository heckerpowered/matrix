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
import net.minecraft.world.entity.player.Player

object HealthStealMagic : Magic(
    MagicDefinition(
        Matrix.identifier("health_steal"),
        8.mana,
        20.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.entityOrNull() ?: return
        val target = invocation.target

        val amount = target.maxHealth * 0.5F
        val healAmount = amount * 0.5F
        caster.heal(healAmount)
        (caster as? Player)?.foodData?.eat(healAmount.toInt(), healAmount)

        if (caster.absorptionAmount >= caster.maxHealth) {
            return
        }

        val absorptionAmount = (caster.absorptionAmount + amount).coerceAtMost(caster.maxHealth)
        caster.absorptionAmount = absorptionAmount // TODO: new absorption manage
    }
}