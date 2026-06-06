/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.entity.rule.EntityUpdateContext
import heckerpowered.matrix.common.entity.rule.EntityUpdateRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes

object AngeredEffect : MobEffect(
    MobEffectCategory.BENEFICIAL,
    0xFF4500
), EntityUpdateRule {
    init {
        RuleRegistry.register<EntityUpdateRule>(this)
        addAttributeModifier(
            Attributes.MOVEMENT_SPEED, Matrix.identifier("angered"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        )
    }

    override fun onUpdate(context: EntityUpdateContext) {
        val entity = context.entity as? LivingEntity ?: return
        if (entity.hasEffect(ModMobEffects.Angered) && entity.tickCount % 10 == 0) {
            entity.level().playSound(null, entity.x, entity.y, entity.z, SoundEvents.WARDEN_HEARTBEAT, entity.soundSource, 5.0F, entity.voicePitch)
        }
    }

    override fun onEffectStarted(effectInstance: MobEffectInstance, entity: LivingEntity) {
        super.onEffectStarted(effectInstance, entity)

        entity.activeEffectsMap
            .asSequence()
            .filter { !it.value.effect.value().isBeneficial }
            .map { it.key }
            .toList()
            .forEach { entity.removeEffect(it) }
    }
}