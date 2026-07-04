/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

import net.minecraft.world.damagesource.DamageSource

class DamageSourceEnvelope(val origin: DamageSource, val rawDamage: Float) : DamageSource(origin.typeHolder(), origin.directEntity, origin.entity) {
    override var isAdditionalDamage: Boolean
        get() = origin.isAdditionalDamage
        set(value) {
            origin.isAdditionalDamage = value
        }
}