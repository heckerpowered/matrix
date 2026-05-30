/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

data class DamageAttemptContext(
    override val target: LivingEntity,
    override val source: DamageSource,
    override val rawDamage: Float,
) : CancellableDamageContext {
    private var cancelled: Boolean = false

    override val isCancelled get() = cancelled

    override fun cancel() {
        cancelled = true
    }
}