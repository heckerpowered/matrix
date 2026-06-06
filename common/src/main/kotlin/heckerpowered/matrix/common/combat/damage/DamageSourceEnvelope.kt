/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

import heckerpowered.matrix.extension.MatrixDamageSource
import net.minecraft.world.damagesource.DamageSource

class DamageSourceEnvelope(
    val origin: DamageSource,
    val rawDamage: Float,
) : DamageSource(origin.typeHolder(), origin.getDirectEntity(), origin.getEntity()), MatrixDamageSource {
    override var isAdditionalDamage: Boolean
        get() = (origin as? MatrixDamageSource)?.isAdditionalDamage ?: false
        set(value) {
            (origin as? MatrixDamageSource)?.isAdditionalDamage = value
        }
}
