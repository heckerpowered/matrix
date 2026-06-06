/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.DamageOutcomeContext
import heckerpowered.matrix.common.combat.damage.DamageOutcomeRule
import heckerpowered.matrix.common.entity.rule.EntityUpdateContext
import heckerpowered.matrix.common.entity.rule.EntityUpdateRule
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.ManaLedger
import heckerpowered.matrix.common.persistent.maxMana
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.core.extension.SequenceExtension.consumeWhile
import heckerpowered.matrix.core.extension.SequenceExtension.drain
import heckerpowered.matrix.core.extension.attackDamage
import heckerpowered.matrix.core.extension.damage
import heckerpowered.matrix.core.utility.getNearestEntities
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player


object AbsolvriftEffect : MobEffect(
    MobEffectCategory.BENEFICIAL,
    0x6A00FF
), EntityUpdateRule, DamageOutcomeRule {
    init {
        RuleRegistry.register<EntityUpdateRule>(this)
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        if (context.source.isAdditionalDamage) return
        val attacker = context.source.entity as? LivingEntity ?: return
        if (!attacker.hasEffect(ModMobEffects.Absolvrift)) return

        val damageSource = attacker.level().damageSources().source(MatrixDamageTypes.magic, attacker)
        damageSource.isAdditionalDamage = true

        val target = context.target
        if (!target.damage(attacker.attackDamage.toFloat() * 0.756F, damageSource)) return
        // val server = target.level().server ?: return
        // val statusEffectInstance = target.getEffect(HEALTH_SHRINK_EFFECT) ?: return
        // for (serverPlayer in server.playerManager.playerList) {
        //     serverPlayer.networkHandler.sendPacket(EntityStatusEffectS2CPacket(target.id, statusEffectInstance, false))
        // }

        if (attacker is Player) {
            attacker.crit(target)
            attacker.magicCrit(target)
        }
        if (attacker is ServerPlayer) {
            ManaLedger.issueMana(attacker, (attacker.maxMana.toDouble() * 0.001).mana)
        }
    }

    override fun onUpdate(context: EntityUpdateContext) {
        val entity = context.entity as? LivingEntity ?: return
        if (!entity.hasEffect(ModMobEffects.Absolvrift)) return
        if (entity.tickCount % 20 != 0) return

        entity.getNearestEntities(6.0)
            .filterIsInstance<LivingEntity>()
            .sortedBy { it.distanceTo(entity) }
            .consumeWhile(5) {
                val damageSource = entity.level().damageSources().source(MatrixDamageTypes.magic, entity)
                val result = it.damage(entity.attackDamage.toFloat() * 0.2F, damageSource)
                if (result) {
                    val amplifier = it.getEffect(ModMobEffects.Absolvrift)?.amplifier ?: 0
                    it.addEffect(MobEffectInstance(ModMobEffects.Absolvrift, 20 * 8, amplifier + 1, false, false, false))

                    if (entity is Player) {
                        entity.crit(it)
                        entity.magicCrit(it)
                    }
                    if (entity is ServerPlayer) {
                        ManaLedger.issueMana(entity, (entity.maxMana.toDouble() * 0.001).mana)
                    }
                }

                result
            }.drain()
    }
}