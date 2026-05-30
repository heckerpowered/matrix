/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.rule

import net.minecraft.world.entity.LivingEntity

data class KnockbackContext(val entity: LivingEntity, val referencePower: Double, val referenceX: Double, val referenceZ: Double) {
    var power = referencePower
    val x = referenceX
    val z = referenceZ

    private var cancelled: Boolean = false
    val isCancelled get() = cancelled

    fun cancel() {
        cancelled = true
    }
}
