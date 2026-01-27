/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.ABSOLVRIFT_EFFECT
import heckerpowered.matrix.common.magic.channel.ChannelQueue
import heckerpowered.matrix.common.magic.core.ExecutionPayload
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.core.extensions.EntityExtensions.damage
import heckerpowered.matrix.core.extensions.LivingEntityExtensions.attackDamage
import heckerpowered.matrix.core.utility.EntitySearch.getNearestEntities
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.server.network.ServerPlayerEntity

object AbsolvriftMagic : Magic(
    MagicDefinition(
        Matrix.identifier("absolvrift"),
        30.mana,
        36.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: ExecutionPayload) {
        super.cast(player, target, sequence, data)

        val damageSource = MemoryWipeMagic.getDamageSource(player, target, data) { target.world.damageSources.create(MatrixDamageTypes.magic, player) }
        val amount = player?.attackDamage?.toFloat() ?: 2.0F

        if (target.damage(amount, damageSource)) {
            player?.addCritParticles(target)
            player?.addEnchantedHitParticles(target)
        }

        target.getNearestEntities(6.0)
            .filter { it.isAttackable && it != player }
            .forEach {
                if (it.damage(amount, damageSource)) {
                    player?.addCritParticles(it)
                    player?.addEnchantedHitParticles(it)
                }
            }
        player?.addStatusEffect(StatusEffectInstance(ABSOLVRIFT_EFFECT, 20 * 25, 0, false, false, true))
    }
}