/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.effect.MatrixStatusEffects.WITHER_ARMOR_CHARGED_EFFECT
import heckerpowered.matrix.common.entity.rule.EntityUpdateContext
import heckerpowered.matrix.common.entity.rule.EntityUpdateRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity

object WitherArmorEnchantment : EntityUpdateRule {
    init {
        RuleRegistry.register<EntityUpdateRule>(this)
    }

    fun onInitialize() {
    }

    override fun onUpdate(context: EntityUpdateContext) {
        val entity = context.entity
        val level = entity.level()
        if (level.isClientSide) return
        if (entity !is LivingEntity) return

        val enchantmentLevel = entity.getEnchantmentLevel(ModEnchantments.witherArmor)
        if (enchantmentLevel <= 0) return
        if (entity.hasEffect(WITHER_ARMOR_CHARGED_EFFECT)) return

        entity.addEffect(MobEffectInstance(WITHER_ARMOR_CHARGED_EFFECT, 20 * 10, 0, true, true))
        if (entity is ServerPlayer) {
            entity.level().server.playerList.sendActiveEffects(entity, entity.connection)
        }
    }
}