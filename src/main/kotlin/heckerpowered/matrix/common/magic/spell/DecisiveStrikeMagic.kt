/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.div
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.magic.Mana.Companion.minus
import heckerpowered.matrix.common.magic.channel.ChannelQueue
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicAvailableStatus
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.persistent.mana
import heckerpowered.matrix.common.persistent.maxMana
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.core.extensions.EntityExtensions.damage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object DecisiveStrikeMagic : Magic(
    MagicDefinition(
        Matrix.identifier("decisive_strike"),
        16.mana,
        60.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: heckerpowered.matrix.common.magic.core.ExecutionPayload) {
        super.cast(player, target, sequence, data)
        val damageSource = MemoryWipeMagic.getDamageSource(player, target, data) { target.world.damageSources.create(MatrixDamageTypes.magic, player) }
        target.timeUntilRegen = 0

        // Each 1% missing mana increases damage by 4%
        val missingMana = if (player == null) {
            .0
        } else {
            ((player.maxMana - player.mana) / player.maxMana).amount
        }

        val baseDamage = 6.0
        val damageIncreaseBasedOnMaxHealth = (target.maxHealth * 0.14).coerceAtLeast(.0)
        val playerAttackDamage = player?.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) ?: .0

        val damageMultiplierBasedOnCost = (missingMana * 0.04).coerceIn(.0..4.0)
        val damageMultiplierBloodPact = if (player?.isBloodPactActive == true) {
            1.0
        } else {
            .0
        }

        val damage = baseDamage + damageIncreaseBasedOnMaxHealth + playerAttackDamage
        val amount = damage * (1 + damageMultiplierBasedOnCost + damageMultiplierBloodPact)

        if (target.damage(amount.toFloat(), damageSource)) {
            player?.addCritParticles(target)
            player?.addEnchantedHitParticles(target)
        }
    }

    override fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?): MagicAvailableStatus {
        val damageSource = if (sequence?.contains<MemoryWipeMagic>() == true) {
            player.world.damageSources.create(MatrixDamageTypes.magic)
        } else {
            player.world.damageSources.create(MatrixDamageTypes.magic, player)
        }

        if (target?.isInvulnerable == true || target?.isInvulnerableTo(damageSource) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}