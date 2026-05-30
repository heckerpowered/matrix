/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import kotlin.math.nextUp

/**
 * Context for a living entity death attempt.
 *
 * This context is created when [entity] is about to die from [referenceDamageSource].
 * Rules may replace [damageSource] to change the effective death source, or set [allow]
 * to `false` to suppress the death.
 *
 * If [allow] is set to `false`, the death is prevented.
 *
 * If the entity's health is already at or below zero, it will be restored to the
 * smallest positive value representable by the health type.
 *
 * In other words, the restored value is the value closest to zero in the direction
 * of positive infinity. Rules that need a different restored health value should
 * set the entity's health explicitly.
 *
 * @property entity The entity whose death is being processed.
 * @property referenceDamageSource The original damage source that triggered this death attempt.
 * @property damageSource The mutable effective damage source used by later rules or death handling.
 * @property allow Whether the death is allowed to proceed.
 */
data class LivingDeathContext(
    val entity: LivingEntity,
    val referenceDamageSource: DamageSource,
) {
    var damageSource = referenceDamageSource
    var allow = true

    /**
     * Applies the current death decision to [entity].
     *
     * If [allow] is `true`, this function does nothing.
     *
     * If [allow] is `false` and the entity's health is at or below zero,
     * its health is restored to the smallest positive float value.
     */
    fun applyDecision() {
        if (allow) return
        if (entity.health > 0.0f) return
        entity.health = 0.0f.nextUp()
    }
}
